package com.rajat.ems.service;

import com.rajat.ems.exception.BadRequestException;
import com.rajat.ems.exception.NotFoundException;
import com.rajat.ems.entity.Employee;
import com.rajat.ems.exception.ValidationError;
import com.rajat.ems.repository.DesignationRepo;
import com.rajat.ems.repository.EmployeeRepo;
import com.rajat.ems.util.MessageConstant;
import com.rajat.ems.model.PutEmployeeRequestEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.rajat.ems.model.PostEmployeeRequestEntity;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmployeeService {

   private final EmployeeRepo employeeRepo;
   private final DesignationRepo designationRepo;
   private final EmployeeValidate employeeValidate;
   private final MessageConstant message;
   private final DesignationValidate designationValidate;

    @Autowired
    public EmployeeService(EmployeeRepo employeeRepo, DesignationRepo designationRepo, EmployeeValidate employeeValidate, MessageConstant message, DesignationValidate designationValidate)
    {
        this.employeeRepo = employeeRepo;
        this.designationRepo = designationRepo;
        this.employeeValidate = employeeValidate;
        this.message=message;
        this.designationValidate=designationValidate;
    }

    /** this will return list of all employees in a sorted order **/
    public List<Employee> getAllEmployees() {
        List<Employee> list = employeeRepo.findAllByOrderByDesignation_levelAscEmployeeNameAsc();
        if (!list.isEmpty()) {
            return list;
        } else {
            throw new NotFoundException(message.getMessage("NO_RECORD_FOUND"));

        }
    }


    /** this will return details of a particular employee in a sorted order **/
    public Map<String, Object> findEmployeeById(int empId) {

        Employee manager;
        List<Employee> colleagues ;
        Map<String, Object> map = new LinkedHashMap<>();


        Employee emp = employeeRepo.findByEmployeeId(empId);
        if (emp==null) {


            throw new NotFoundException(message.getMessage("NOT_FOUND",empId));
        } else {
            map.put("employee", emp);

            if (emp.getParentId() != null) {
                manager = employeeRepo.findByEmployeeId(emp.getParentId());
                map.put("manager", manager);

                colleagues = employeeRepo.findAllByParentIdAndEmployeeIdIsNotOrderByDesignation_levelAscEmployeeNameAsc(emp.getParentId(), emp.getEmployeeId());
                if (!colleagues.isEmpty())
                    map.put("colleagues", colleagues);
            }

            List<Employee> reporting = employeeRepo.findAllByParentIdAndEmployeeIdIsNotOrderByDesignation_levelAscEmployeeNameAsc(emp.getEmployeeId(), emp.getEmployeeId());
            if (!reporting.isEmpty())
                map.put("subordinates", reporting);


        }
        return map;

    }

    /** this method will update the supervisor of all subordinates of the oldId **/
    private void updateSupervisor(Integer oldId, Integer newId) {
        List<Employee> subordinates = employeeRepo.findAllByParentId(oldId);
        if (!subordinates.isEmpty()) {
            for (Employee emp : subordinates) {
                emp.setParentId(newId);                                                      // changing the supervisor id of subordinates of employee by oldId
                employeeRepo.save(emp);
            }

        }
    }


    /** this method wil delete the employee with the given Id otherwise respective response **/
    public void deleteEmployee(int id) {
        try{
            employeeValidate.validateId(id);
        }
        catch(ValidationError error)
        {
            throw new NotFoundException(message.getMessage("VALIDATION_ERROR_INVALID_ID",error.cause));
        }


            Employee emp = employeeRepo.findByEmployeeId(id);
            if (emp.getDesignationName().equals(this.getHighestPosition().designation.getDesignationName())) {
                if (!employeeRepo.findAllByParentId(emp.getEmployeeId()).isEmpty()) {                                              // checking if there are any subordinates of the director
                    throw new BadRequestException(message.getMessage("UNABLE_TO_DELETE_DIRECTOR"));
                    //return new ResponseEntity<>(message.getMessage("UNABLE_TO_DELETE_DIRECTOR"), HttpStatus.BAD_REQUEST);    // Not able to delete as there are some subordinates of director are present
                } else {

                    employeeRepo.delete(emp);                                                               //Able to delete as there is no subordinates of director
                }
            } else {

                Integer parentId = emp.getParentId();
                if(parentId==null){
                    throw new BadRequestException(message.getMessage("ERROR_DELETING_EMPLOYEE_WITHOUT_PARENT"));
                }
                else {
                    this.updateSupervisor(id, parentId);                                                         // updating the supervisor Id of the subordinates of the oldEmployee
                    employeeRepo.delete(emp);
                }
            }
    }


    /** this method will serve as false case of put  **/
    public Map<String, Object> employeeUpdate(int oldId, PutEmployeeRequestEntity emp) {
        Employee employee = employeeRepo.findByEmployeeId(oldId);


        employeeValidate.validateBody(emp.getName(),emp.getManagerId(),emp.getJobTitle());

        try {
            employeeValidate.validateName(emp.getName(),false);
        }
        catch(ValidationError error){

            throw new BadRequestException(message.getMessage("VALIDATION_ERROR_INVALID_NAME",error.cause));
        }


        if (emp.getManagerId() != null) {

            if (employeeRepo.findByEmployeeId(oldId).designation == this.getHighestPosition().designation)
                throw new BadRequestException(message.getMessage("UPDATING_DIRECTOR"));
                                  // badRequest as user is trying to update the supervisor of the director
            if (StringUtils.isEmpty(emp.getJobTitle())) {
                if (employeeValidate.parentPossible(employee, emp.getManagerId())) {                                        // checking if the new supervisor is valid or not
                    employee.setParentId(emp.getManagerId());

                } else
                    throw new BadRequestException(message.getMessage("INVALID_SUPERVISOR"));

            } else if (employeeValidate.designationExist(emp.getJobTitle())) {                                                                   // if jobTitle and supervisor id both are not null then check if the new inputs are valid or not
                employee.designation = designationRepo.findByDesignationNameLike(emp.getJobTitle());
                if (employeeValidate.parentPossible(employee, emp.getManagerId())) {
                    employee.setParentId(emp.getManagerId());                                                                         // saving the employee details
                    employeeRepo.save(employee);
                } else
                    throw new BadRequestException(message.getMessage("INVALID_ENTRY"));

            }
        }

        if (!StringUtils.isEmpty(emp.getJobTitle())) {
            if (employeeRepo.findByEmployeeId(oldId).designation.getDesignationId() == designationValidate.getHighestDesignation().getDesignationId() && (!emp.getJobTitle().equals(designationValidate.getHighestDesignation().getDesignationName()))) {
                throw new BadRequestException(message.getMessage("UPDATING_DIRECTOR"));                                                        //badRequest user is trying to update jobTitle of director
            }
            if (emp.getManagerId() == null) {
                if (employeeValidate.designationChange(employee, emp.getJobTitle())) {
                    employee.designation = designationRepo.findByDesignationNameLike(emp.getJobTitle());

                }
                else
                    throw new BadRequestException(message.getMessage("INVALID_DESIGNATION"));


            } else if (employeeValidate.empExist(emp.getManagerId())) {
                employee.setParentId(emp.getManagerId());
                employeeRepo.save(employee);
                if (employeeValidate.designationChange(employee, emp.getJobTitle())) {
                    employee.designation = designationRepo.findByDesignationNameLike(emp.getJobTitle());

                } else
                    throw new BadRequestException(message.getMessage("INVALID_ENTRY"));

            }
        }

        if (!StringUtils.isEmpty(emp.getName())) {
            try{
                employeeValidate.validateName(emp.getName(),false);
            }
            catch(ValidationError error)
            {
                throw new BadRequestException(message.getMessage("VALIDATION_ERROR_INVALID_NAME",error.cause));
            }
            employee.setEmployeeName(emp.getName());
        }

        employeeRepo.save(employee);                                                                                                   //saving the employee details
        return this.findEmployeeById(employee.getEmployeeId());

    }

    /** this method will serve as false case of put **/
    public Map<String, Object> replaceEmployee(int empId, PutEmployeeRequestEntity emp) {
            employeeValidate.validateDesignation(emp.getJobTitle());

        try {
            employeeValidate.validateName(emp.getName(),true);
        }
        catch(ValidationError error){

            throw new BadRequestException(message.getMessage("VALIDATION_ERROR_INVALID_NAME",error.cause));
        }



        if (emp.getManagerId() != null) {
            if (!employeeValidate.empExist(emp.getManagerId())) {
                throw new BadRequestException(message.getMessage("INVALID_SUPERVISOR"));
            }
            Employee newEmployee = new Employee();
            newEmployee.designation = designationRepo.findByDesignationNameLike(emp.getJobTitle());
            if (employeeValidate.parentPossible(newEmployee, emp.getManagerId())) {                                // checking if the new supervisor is valid or not
                newEmployee.setEmployeeName(emp.getName());
                newEmployee.setParentId(emp.getManagerId());                                                 // saving the new employee
                employeeRepo.save(newEmployee);
                this.updateSupervisor(empId, newEmployee.getEmployeeId());
                employeeRepo.delete(employeeRepo.findByEmployeeId(empId));
                return this.findEmployeeById(newEmployee.getEmployeeId());

            } else throw new BadRequestException(message.getMessage("INVALID_SUPERVISOR"));             // if the level of supervisor and subordinates is not valid then return badRequest
        } else if (employeeValidate.designationValid(employeeRepo.findByEmployeeId(empId), emp.getJobTitle()) && emp.getManagerId() == null) {        // checking if the designation  entered is valid or not

            Employee newEmployee = new Employee(designationRepo.findByDesignationNameLike(emp.getJobTitle()), employeeRepo.findByEmployeeId(empId).getParentId(), emp.getName());
            employeeRepo.save(newEmployee);
            this.updateSupervisor(empId, newEmployee.getEmployeeId());
            employeeRepo.delete(employeeRepo.findByEmployeeId(empId));
            return this.findEmployeeById(newEmployee.getEmployeeId());

        } else throw new BadRequestException(message.getMessage("INVALID_DESIGNATION"));
    }

    public Employee addEmployee(PostEmployeeRequestEntity employeeToAdd) {
        employeeValidate.validateDesignation(employeeToAdd.getJobTitle());                                                    //entered designation does not exist
        Employee highestEmployee = this.getHighestPosition();
        if (!(employeeValidate.empExist(employeeToAdd.getManagerId())) && designationRepo.findByDesignationNameLike(employeeToAdd.getJobTitle()).getDesignationId() != designationValidate.getHighestDesignation().getDesignationId()) {                           //supervisor null and designation is not director
            System.err.println("first");
            throw new BadRequestException(message.getMessage("INVALID_SUPERVISOR"));

        }
        try{
            employeeValidate.validateName(employeeToAdd.getName(),true);
        }
        catch (ValidationError error)
        {
            throw new BadRequestException(message.getMessage("VALIDATION_ERROR_INVALID_NAME",error.cause));
        }
        if (!employeeValidate.empExist(employeeToAdd.getManagerId())&& highestEmployee.designation.getLevel()>designationRepo.findByDesignationNameLike(employeeToAdd.getJobTitle()).getLevel()) {           // if the supervisor id is null or negative then it will check the number of employees in the organization if the number is zero then it will add only if the employeeToAdd is highest designation

                if (designationRepo.findByDesignationNameLike(employeeToAdd.getJobTitle()).getDesignationId() == designationValidate.getHighestDesignation().getDesignationId()) {
                    Employee newEmployee = new Employee(designationRepo.findByDesignationNameLike(employeeToAdd.getJobTitle()), employeeToAdd.getManagerId(), employeeToAdd.getName());
                    employeeRepo.save(newEmployee);
                    highestEmployee.setParentId(newEmployee.getEmployeeId());
                    employeeRepo.save(highestEmployee);

                    return newEmployee;
                } else {
                    throw new BadRequestException(message.getMessage("INVALID_ENTRY"));
                }

        } else{

            Employee newEmployee = new Employee(designationRepo.findByDesignationNameLike(employeeToAdd.getJobTitle()), employeeToAdd.getManagerId(), employeeToAdd.getName());
            if(employeeToAdd.getManagerId()!=null&&employeeValidate.isSmallerThanParent(newEmployee,employeeToAdd.getJobTitle()))                                          // checking if the designation is valid against supervisor or not
            {
                employeeRepo.save(newEmployee);                                                                                                                                  // saving the new employeeToAdd
                return newEmployee;
            }


        else {

                throw new BadRequestException(message.getMessage("INVALID_SUPERVISOR"));
            }

        }
    }

    public Employee getHighestPosition(){
        return employeeRepo.findFirstByOrderByDesignation_levelAscEmployeeNameAsc();
    }
}

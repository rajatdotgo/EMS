package com.rajat.ems.service;

import com.rajat.ems.exception.BadRequestException;
import com.rajat.ems.exception.NotFoundException;
import com.rajat.ems.model.Employee;
import com.rajat.ems.repository.DesignationRepo;
import com.rajat.ems.repository.EmployeeRepo;
import com.rajat.ems.util.MessageConstant;
import com.rajat.ems.model.PutEmployeeRequestEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @Autowired
    public EmployeeService(EmployeeRepo employeeRepo, DesignationRepo designationRepo, EmployeeValidate employeeValidate, MessageConstant message)
    {
        this.employeeRepo = employeeRepo;
        this.designationRepo = designationRepo;
        this.employeeValidate = employeeValidate;
        this.message=message;
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


            throw new NotFoundException(message.getMessage("NO_RECORD_FOUND"));
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
        if (!employeeValidate.empExist(id)) {
            throw new NotFoundException(message.getMessage("NO_RECORD_FOUND"));
        } else {
            Employee emp = employeeRepo.findByEmployeeId(id);
            if (emp.getDesignationName().equals("Director")) {
                if (!employeeRepo.findAllByParentId(emp.getEmployeeId()).isEmpty()) {                                              // checking if there are any subordinates of the director
                    throw new BadRequestException(message.getMessage("UNABLE_TO_DELETE_DIRECTOR"));
                    //return new ResponseEntity<>(message.getMessage("UNABLE_TO_DELETE_DIRECTOR"), HttpStatus.BAD_REQUEST);    // Not able to delete as there are some subordinates of director are present
                } else {

                    employeeRepo.delete(emp);
                    //return new ResponseEntity<>(message.getMessage("DELETED"), HttpStatus.NO_CONTENT);                     //Able to delete as there is no subordinates of director
                }
            } else {
                int parentId = emp.getParentId();
                this.updateSupervisor(id, parentId);                                                         // updating the supervisor Id of the subordinates of the oldEmployee
                employeeRepo.delete(emp);
                //return new ResponseEntity<>(message.getMessage("DELETED"), HttpStatus.NO_CONTENT);
            }
        }

    }


    /** this method will serve as false case of put  **/
    public Map<String, Object> employeeUpdate(int oldId, PutEmployeeRequestEntity emp) {
        Employee employee = employeeRepo.findByEmployeeId(oldId);


        if (StringUtils.isEmpty(emp.getName()) && (emp.getManagerId() == null) && StringUtils.isEmpty(emp.getJobTitle())) {
           // return new ResponseEntity<>(message.getMessage("INSUFFICIENT_DATA"), HttpStatus.BAD_REQUEST);          // returning badRequest as user entered nothing to be updated
            throw new BadRequestException(message.getMessage("INSUFFICIENT_DATA"));
        }


        if (emp.getManagerId() != null) {

            if (employeeRepo.findByEmployeeId(oldId).designation.getDesignationId() == 1)
                throw new BadRequestException(message.getMessage("UPDATING_DIRECTOR"));
                //return new ResponseEntity<>(message.getMessage("UPDATING_DIRECTOR"), HttpStatus.BAD_REQUEST);                  // badRequest as user is trying to update the supervisor of the director
            if (StringUtils.isEmpty(emp.getJobTitle())) {
                if (employeeValidate.parentPossible(employee, emp.getManagerId())) {                                        // checking if the new supervisor is valid or not
                    employee.setParentId(emp.getManagerId());

                } else
                    throw new BadRequestException(message.getMessage("INVALID_SUPERVISOR"));
                   // return new ResponseEntity<>(message.getMessage("INVALID_SUPERVISOR"), HttpStatus.BAD_REQUEST);
            } else if (employeeValidate.designationExist(emp.getJobTitle())) {                                                                   // if jobTitle and supervisor id both are not null then check if the new inputs are valid or not
                employee.designation = designationRepo.findByDesignationNameLike(emp.getJobTitle());
                if (employeeValidate.parentPossible(employee, emp.getManagerId())) {
                    employee.setParentId(emp.getManagerId());                                                                         // saving the employee details
                    employeeRepo.save(employee);
                } else
                    throw new BadRequestException(message.getMessage("INVALID_ENTRY"));
                   // return new ResponseEntity<>(message.getMessage("INVALID_ENTRY"), HttpStatus.BAD_REQUEST);
            }
        }

        if (!StringUtils.isEmpty(emp.getJobTitle())) {
            if (employeeRepo.findByEmployeeId(oldId).designation.getDesignationId() == 1 && (!emp.getJobTitle().equals("Director"))) {
                throw new BadRequestException(message.getMessage("UPDATING_DIRECTOR"));
               // return new ResponseEntity<>(message.getMessage("UPDATING_DIRECTOR"), HttpStatus.BAD_REQUEST);                  //badRequest user is trying to update jobTitle of director
            }
            if (emp.getManagerId() == null) {
                if (employeeValidate.designationChange(employee, emp.getJobTitle())) {
                    employee.designation = designationRepo.findByDesignationNameLike(emp.getJobTitle());

                }
                else
                    throw new BadRequestException(message.getMessage("INVALID_DESIGNATION"));
                   // return new ResponseEntity<>(message.getMessage("INVALID_DESIGNATION"), HttpStatus.BAD_REQUEST);
            } else if (employeeValidate.empExist(emp.getManagerId())) {
                employee.setParentId(emp.getManagerId());
                employeeRepo.save(employee);
                if (employeeValidate.designationChange(employee, emp.getJobTitle())) {
                    employee.designation = designationRepo.findByDesignationNameLike(emp.getJobTitle());

                } else
                    throw new BadRequestException(message.getMessage("INVALID_ENTRY"));
                   // return new ResponseEntity<>(message.getMessage("INVALID_ENTRY"), HttpStatus.BAD_REQUEST);
            }
        }

        if (!StringUtils.isEmpty(emp.getName())) {
            employee.setEmployeeName(emp.getName());
        }

        employeeRepo.save(employee);                                                                                                   //saving the employee details
        return this.findEmployeeById(employee.getEmployeeId());

    }

    /** this method will serve as false case of put **/
    public Map<String, Object> replaceEmployee(int empId, PutEmployeeRequestEntity emp) {
        if (!employeeValidate.designationExist(emp.getJobTitle()))
            throw new BadRequestException(message.getMessage("INVALID_DESIGNATION"));
        if (StringUtils.isEmpty(emp.getName()))
            throw new BadRequestException(message.getMessage("INVALID_NAME"));

       else  if (emp.getManagerId() != null) {
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
                //return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
            } else throw new BadRequestException(message.getMessage("INVALID_SUPERVISOR"));             // if the level of supervisor and subordinates is not valid then return badRequest
        } else if (employeeValidate.designationValid(employeeRepo.findByEmployeeId(empId), emp.getJobTitle()) && emp.getManagerId() == null) {        // checking if the designation  entered is valid or not

            Employee newEmployee = new Employee(designationRepo.findByDesignationNameLike(emp.getJobTitle()), employeeRepo.findByEmployeeId(empId).getParentId(), emp.getName());
            employeeRepo.save(newEmployee);
            this.updateSupervisor(empId, newEmployee.getEmployeeId());
            employeeRepo.delete(employeeRepo.findByEmployeeId(empId));
            return this.findEmployeeById(newEmployee.getEmployeeId());
           // return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
        } else throw new BadRequestException(message.getMessage("INVALID_DESIGNATION"));
    }

    public Employee addEmployee(PostEmployeeRequestEntity employee) {
        if (!employeeValidate.designationExist(employee.getJobTitle())) {
            throw new BadRequestException(message.getMessage("INVALID_SUPERVISOR"));                                                        //entered designation does not exist
        } else if (!(employeeValidate.empExist(employee.getManagerId())) && designationRepo.findByDesignationNameLike(employee.getJobTitle()).getDesignationId() != 1) {                           //supervisor null and designation is not director
            throw new BadRequestException(message.getMessage("INVALID_SUPERVISOR"));

        } else if (StringUtils.isEmpty(employee.getName())|| employee.getName().matches(".*\\d.*")) {
            throw new BadRequestException(message.getMessage("INVALID_NAME"));                                                           // name containing numbers
        }
        if (!employeeValidate.empExist(employee.getManagerId())) {           // if the supervisor id is null or negative then it will check the number of employees in the organization if the number is zero then it will add only if the employee is director
            if (employeeRepo.findAll().isEmpty()) {
                if (designationRepo.findByDesignationNameLike(employee.getJobTitle()).getDesignationId() == 1) {
                    Employee newEmployeee = new Employee(designationRepo.findByDesignationNameLike(employee.getJobTitle()), employee.getManagerId(), employee.getName());
                    employeeRepo.save(newEmployeee);

                    return newEmployeee;
                } else {
                    throw new BadRequestException(message.getMessage("INVALID_ENTRY"));
                }
            } else {
                throw new BadRequestException(message.getMessage("INVALID_SUPERVISOR"));
            }
        } else {

            if (employeeRepo.findByEmployeeId(employee.getManagerId()).designation.getLevel() < designationRepo.findByDesignationNameLike(employee.getJobTitle()).getLevel()) {               // checking if the designation is valid against supervisor or not
                Employee newEmployee = new Employee(designationRepo.findByDesignationNameLike(employee.getJobTitle()), employee.getManagerId(), employee.getName());
                employeeRepo.save(newEmployee);                                                                                                                                  // saving the new employee
                return newEmployee;
            } else {
                throw new BadRequestException(message.getMessage("INVALID_SUPERVISOR"));
            }

        }


    }
}

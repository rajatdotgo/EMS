package com.rajat.demoemp1.service;

import com.rajat.demoemp1.model.Employee;
import com.rajat.demoemp1.repository.DesignationRepo;
import com.rajat.demoemp1.repository.EmployeeRepo;
import com.rajat.demoemp1.util.MessageConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.rajat.demoemp1.model.PutRequest;
import com.rajat.demoemp1.model.PostRequest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmployeeService {

    @Autowired
    EmployeeRepo empRepo;
    @Autowired
    DesignationRepo degRepo;
    @Autowired
    EmployeeValidate empValidate;
    @Autowired
    MessageConstant message;

    public ResponseEntity getAll() {                                                                            /************* this will return list of all employees in a sorted order  *************/
        List<Employee> list = empRepo.findAllByOrderByDesignation_levelAscEmpNameAsc();
        if (!list.isEmpty()) {
            return new ResponseEntity<>(list, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(message.getMessage("NO_RECORD_FOUND"), HttpStatus.NOT_FOUND);
        }
    }


    public ResponseEntity findParticular(int empId) {                                                        /*************** this will return details of a particular employee in a sorted order *****************/

        Employee manager = null;
        List<Employee> colleagues = null;
        Map<String, Object> map = new LinkedHashMap<>();
        HttpStatus status = null;

        Employee emp = empRepo.findByEmpId(empId);
        if (!empValidate.empExist(empId)) {


            return new ResponseEntity<>(message.getMessage("NO_RECORD_FOUND"), HttpStatus.NOT_FOUND);
        } else {
            map.put("employee", emp);

            if (emp.getParentId() != null) {
                manager = empRepo.findByEmpId(emp.getParentId());
                map.put("manager", manager);

                colleagues = empRepo.findAllByParentIdAndEmpIdIsNotOrderByDesignation_levelAscEmpNameAsc(emp.getParentId(), emp.getEmpId());
                if (!colleagues.isEmpty())
                    map.put("colleagues", colleagues);
            }

            List<Employee> reporting = empRepo.findAllByParentIdAndEmpIdIsNotOrderByDesignation_levelAscEmpNameAsc(emp.getEmpId(), emp.getEmpId());
            if (!reporting.isEmpty())
                map.put("subordinates", reporting);
            status = HttpStatus.OK;

        }
        return new ResponseEntity<>(map, status);

    }

    public void updateSupervisor(Integer oldId, Integer newId) {                                          /************* this method will update the supervisor of all subordinates of the oldId *********/
        List<Employee> subordinates = empRepo.findAllByParentId(oldId);
        if (!subordinates.isEmpty()) {
            for (Employee emp : subordinates) {
                emp.setParentId(newId);                                                      // changing the supervisor id of subordinates of employee by oldId
                empRepo.save(emp);
            }

        }
    }


    public ResponseEntity deleteEmployee(int id) {                                          /*********************  this method wil delete the employee with the given Id otherwise respective response **************/
        if (!empValidate.empExist(id)) {
            return new ResponseEntity<>(message.getMessage("NO_RECORD_FOUND"), HttpStatus.NOT_FOUND);
        } else {
            Employee emp = empRepo.findByEmpId(id);
            if (emp.getDesgName().equals("Director")) {
                if (!empRepo.findAllByParentId(emp.getEmpId()).isEmpty()) {                                              // checking if there are any subordinates of the director
                    return new ResponseEntity<>(message.getMessage("UNABLE_TO_DELETE_DIRECTOR"), HttpStatus.BAD_REQUEST);    // Not able to delete as there are some subordinates of director are present
                } else {

                    empRepo.delete(emp);
                    return new ResponseEntity<>(message.getMessage("DELETED"), HttpStatus.NO_CONTENT);                     //Able to delete as there is no subordinates of director
                }
            } else {
                int parentId = emp.getParentId();
                this.updateSupervisor(id, parentId);                                                         // updating the supervisor Id of the subordinates of the oldEmployee
                empRepo.delete(emp);
                return new ResponseEntity<>(message.getMessage("DELETED"), HttpStatus.NO_CONTENT);
            }
        }

    }


    public ResponseEntity employeeUpdate(int oldId, PutRequest emp) {                                                 /******************* this method will serve as false case of put  ***********************/
        Employee employee = empRepo.findByEmpId(oldId);


        if ((emp.getName() == null || emp.getName().equals("")) && (emp.getManagerId() == null) && (emp.getJobTitle() == null || emp.getJobTitle().equals(""))) {
            return new ResponseEntity<>(message.getMessage("INSUFFICIENT_DATA"), HttpStatus.BAD_REQUEST);          // returning badRequest as user entered nothing to be updated
        }


        if (emp.getManagerId() != null) {

            if (empRepo.findByEmpId(oldId).designation.getDesId() == 1)
                return new ResponseEntity<>(message.getMessage("UPDATING_DIRECTOR"), HttpStatus.BAD_REQUEST);                  // badRequest as user is trying to update the supervisor of the director
            if (emp.getJobTitle() == null) {
                if (empValidate.parentPossible(employee, emp.getManagerId())) {                                        // checking if the new supervisor is valid or not
                    employee.setParentId(emp.getManagerId());

                } else
                    return new ResponseEntity<>(message.getMessage("INVALID_SUPERVISOR"), HttpStatus.BAD_REQUEST);
            } else if (empValidate.desExist(emp.getJobTitle())) {                                                                   // if jobTitle and supervisor id both are not null then check if the new inputs are valid or not
                employee.designation = degRepo.findByDesgNameLike(emp.getJobTitle());
                if (empValidate.parentPossible(employee, emp.getManagerId())) {
                    employee.setParentId(emp.getManagerId());                                                                         // saving the employee details
                    empRepo.save(employee);
                } else
                    return new ResponseEntity<>(message.getMessage("INVALID_ENTRY"), HttpStatus.BAD_REQUEST);
            }
        }

        if (emp.getJobTitle() != null &&( !emp.getJobTitle().equals(""))) {
            if (empRepo.findByEmpId(oldId).designation.getDesId() == 1 && (!emp.getJobTitle().equals("Director"))) {
                return new ResponseEntity<>(message.getMessage("UPDATING_DIRECTOR"), HttpStatus.BAD_REQUEST);                  //badRequest user is trying to update jobTitle of director
            }
            if (emp.getManagerId() == null) {
                if (empValidate.designationChange(employee, emp.getJobTitle())) {
                    employee.designation = degRepo.findByDesgNameLike(emp.getJobTitle());

                }
                else
                    return new ResponseEntity<>(message.getMessage("INVALID_DESIGNATION"), HttpStatus.BAD_REQUEST);
            } else if (empValidate.empExist(emp.getManagerId())) {
                employee.setParentId(emp.getManagerId());
                empRepo.save(employee);
                if (empValidate.designationChange(employee, emp.getJobTitle())) {
                    employee.designation = degRepo.findByDesgNameLike(emp.getJobTitle());

                } else
                    return new ResponseEntity<>(message.getMessage("INVALID_ENTRY"), HttpStatus.BAD_REQUEST);
            }
        }

        if (emp.getName() != null && (!emp.getName().trim().equals(""))) {
            employee.setEmpName(emp.getName());
        }

        empRepo.save(employee);                                                                                                   //saving the employee details
        ResponseEntity response = this.findParticular(employee.getEmpId());
        return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
    }

    public ResponseEntity replaceEmployee(int empId, PutRequest emp) {                                                     /******************* this method will serve as false case of put  ***********************/
        if (!empValidate.desExist(emp.getJobTitle()))
            return new ResponseEntity<>(message.getMessage("INVALID_DESIGNATION"), HttpStatus.BAD_REQUEST);
        if (emp.getName() == null || emp.getName().trim().equals(""))
            return new ResponseEntity<>(message.getMessage("INVALID_NAME"), HttpStatus.BAD_REQUEST);

       else  if (emp.getManagerId() != null) {
            if (!empValidate.empExist(emp.getManagerId())) {
                return new ResponseEntity<>(message.getMessage("INVALID_SUPERVISOR"), HttpStatus.BAD_REQUEST);
            }
            Employee newEmployee = new Employee();
            newEmployee.designation = degRepo.findByDesgNameLike(emp.getJobTitle());
            if (empValidate.parentPossible(newEmployee, emp.getManagerId())) {                                // checking if the new supervisor is valid or not
                newEmployee.setEmpName(emp.getName());
                newEmployee.setParentId(emp.getManagerId());                                                 // saving the new employee
                empRepo.save(newEmployee);
                this.updateSupervisor(empId, newEmployee.getEmpId());
                empRepo.delete(empRepo.findByEmpId(empId));
                ResponseEntity response = this.findParticular(newEmployee.getEmpId());
                return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
            } else return new ResponseEntity<>(message.getMessage("INVALID_SUPERVISOR"), HttpStatus.BAD_REQUEST);             // if the level of supervisor and subordinates is not valid then return badRequest
        } else if (empValidate.designationValid(empRepo.findByEmpId(empId), emp.getJobTitle()) && emp.getManagerId() == null) {        // checking if the designation  entered is valid or not

            Employee newEmployee = new Employee(degRepo.findByDesgNameLike(emp.getJobTitle()), empRepo.findByEmpId(empId).getParentId(), emp.getName());
            empRepo.save(newEmployee);
            this.updateSupervisor(empId, newEmployee.getEmpId());
            empRepo.delete(empRepo.findByEmpId(empId));
            ResponseEntity response = this.findParticular(newEmployee.getEmpId());
            return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
        } else return new ResponseEntity<>(message.getMessage("INVALID_DESIGNATION"), HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity addEmployee(PostRequest employee) {
        if (!empValidate.desExist(employee.getJobTitle())) {
            return new ResponseEntity<>(message.getMessage("INVALID_DESIGNATION"), HttpStatus.BAD_REQUEST);                                                        //entered designation does not exist
        } else if (!(empValidate.empExist(employee.getManagerId())) && degRepo.findByDesgNameLike(employee.getJobTitle()).getDesId() != 1) {                           //supervisor null and designation is not director
            return new ResponseEntity<>(message.getMessage("INVALID_SUPERVISOR"), HttpStatus.BAD_REQUEST);

        } else if (employee.getName() == null || employee.getName().matches(".*\\d.*")) {
            return new ResponseEntity<>(message.getMessage("INVALID_NAME"), HttpStatus.BAD_REQUEST);                                                           // name containing numbers
        }
        if (!empValidate.empExist(employee.getManagerId())) {           // if the supervisor id is null or negative then it will check the number of employees in the organization if the number is zero then it will add only if the employee is director
            if (empRepo.findAll().isEmpty()) {
                if (degRepo.findByDesgNameLike(employee.getJobTitle()).getDesId() == 1) {
                    Employee emp = new Employee(degRepo.findByDesgNameLike(employee.getJobTitle()), employee.getManagerId(), employee.getName());
                    empRepo.save(emp);

                    return new ResponseEntity<>(emp, HttpStatus.CREATED);
                } else {
                    return new ResponseEntity<>(message.getMessage("INVALID_ENTRY"), HttpStatus.BAD_REQUEST);
                }
            } else {
                return new ResponseEntity<>(message.getMessage("INVALID_SUPERVISOR"), HttpStatus.BAD_REQUEST);
            }
        } else {

            if (empRepo.findByEmpId(employee.getManagerId()).designation.getLevel() < degRepo.findByDesgNameLike(employee.getJobTitle()).getLevel()) {               // checking if the designation is valid against supervisor or not
                Employee emp = new Employee(degRepo.findByDesgNameLike(employee.getJobTitle()), employee.getManagerId(), employee.getName());
                empRepo.save(emp);                                                                                                                                  // saving the new employee
                return new ResponseEntity<>(emp, HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>(message.getMessage("INVALID_SUPERVISOR"), HttpStatus.BAD_REQUEST);
            }

        }


    }
}

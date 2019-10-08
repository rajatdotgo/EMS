package com.rajat.demoemp1.controller;

import com.rajat.demoemp1.repository.DesignationRepo;
import com.rajat.demoemp1.repository.EmployeeRepo;
import com.rajat.demoemp1.model.Designation;
import com.rajat.demoemp1.model.Employee;
import com.rajat.demoemp1.model.putRequest;
import com.rajat.demoemp1.model.postRequest;
import com.rajat.demoemp1.service.employeeService;
import com.rajat.demoemp1.service.employeeValidate;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ControllerClass {

    @Autowired
    EmployeeRepo empRepo;
    @Autowired
    DesignationRepo degRepo;
    @Autowired
    employeeService empService;
    @Autowired
    employeeValidate empValidate;

    @GetMapping("/rest/employees")
    @ApiOperation(value="Finds all the employees sorted according to their designation")
    public ResponseEntity allEmployees()
    {
       return empService.getAll();
    }

    @GetMapping("/rest/employees/{empId}")
    @ApiOperation(value="Finds an employee by employee id otherwise suitable response")
    public ResponseEntity findParticular(@ApiParam(value = "Employee unique id for the details you need to retrieve",required = true) @PathVariable("empId") int empId)
    {
        return empService.findParticular(empId);

    }


    @PostMapping(path = "/rest/employees")
    @ApiOperation(value="Adds a new employee in the organisation")
    public ResponseEntity<String> saveData(@RequestBody postRequest employee)
    {

        String empName=employee.getEmpName();
        String desg=employee.getEmpDesg().toUpperCase();
        Integer parent=employee.getParentId();

        Designation designation=degRepo.findByDesgNameLike(desg);
        float childLevel=designation.getLevel();
        if(empRepo.findByEmpId(parent)==null)
        {

            return new ResponseEntity<>( "No supervisor found of given id", HttpStatus.BAD_REQUEST);
        }
        if(!(parent==null&& empRepo.findByEmpId(parent)==null))
        {
            Employee employee1=empRepo.findByEmpId(parent);
            float parLevel=employee1.designation.level;
            if (parLevel < childLevel) {
                Employee emp = new Employee(designation, parent, empName);
                empRepo.save(emp);

                return new ResponseEntity<>( "Data Saved", HttpStatus.OK);
            } else {

                return new ResponseEntity<>( desg+" can not report to "+empRepo.findByEmpId(parent).getDesgName(),HttpStatus.BAD_REQUEST);
            }

        }
        else if( empRepo.findAll().size()<=0)
        {
            if(desg=="DIRECTOR") {
                Employee emp = new Employee(designation, parent, empName);
                empRepo.save(emp);

                return new ResponseEntity<>("Data Saved",HttpStatus.OK);
            }
            else
            {
                return new ResponseEntity<>("Unable to find any DIRECTOR in the organization at the moment",HttpStatus.BAD_REQUEST);
            }
        }
        else
        {
            Employee emp = empRepo.findByParentId(null);
            if(emp==null)
            {
                Employee emp1 = new Employee(designation, parent, empName);
                empRepo.save(emp1);

                return new ResponseEntity<>("Data Saved",HttpStatus.OK);
            }
            else {

                return new ResponseEntity<>("please enter valid supervisor id",HttpStatus.BAD_REQUEST);
            }
        }
    }

    @PutMapping("/rest/employees/{empId}")
    @ApiOperation(value="Updates a particular employee by Id ")
    public ResponseEntity putData(@ApiParam(value = "Employee unique id whose details you need to update",required = true)@PathVariable("empId") int empId, @RequestBody putRequest emp) {

        ResponseEntity re= null;
        if (!empValidate.empExist(empId)) {
            re =  new ResponseEntity("No employee found by given id", HttpStatus.NOT_FOUND);
        }
        // when replace is true
        if (emp.isReplace()) {
           re  = empService.replaceEmployee(empId,emp);
        }
        // when replace is false
        else {
            re  = empService.employeeUpdate(empId, emp);
        }
        return re;
    }
    @DeleteMapping("/rest/employees/{empId}")
    @ApiOperation(value="Delete an employee by id otherwise suitable response")
    public ResponseEntity deleteEmployee(@ApiParam(value = "Employee unique id whom you want to delete",required = true)@PathVariable("empId") int empId)
    {
       return  empService.deleteEmployee(empId);
    }

}


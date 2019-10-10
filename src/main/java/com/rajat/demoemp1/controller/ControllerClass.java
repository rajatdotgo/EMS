package com.rajat.demoemp1.controller;

import com.rajat.demoemp1.repository.DesignationRepo;
import com.rajat.demoemp1.repository.EmployeeRepo;
import com.rajat.demoemp1.model.PutRequest;
import com.rajat.demoemp1.model.PostRequest;
import com.rajat.demoemp1.service.employeeService;
import com.rajat.demoemp1.service.employeeValidate;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<String> saveData(@RequestBody PostRequest employee)
    {
        return empService.addEmployee(employee);
    }

    @PutMapping("/rest/employees/{empId}")
    @ApiOperation(value="Updates a particular employee by Id ")
    public ResponseEntity putData(@ApiParam(value = "Employee unique id whose details you need to update",required = true)@PathVariable("empId") int empId, @RequestBody PutRequest emp) {

        ResponseEntity re= null;
        if (!empValidate.empExist(empId)) {
            re =  new ResponseEntity("No employee found by given id", HttpStatus.NOT_FOUND);
        }
        // when replace is true
       else if (emp.isReplace()) {
           re  = empService.replaceEmployee(empId,emp);
        }
        // when replace is false
        else {
            re  = empService.employeeUpdate(empId, emp);
        }
        return re;
    }
    @DeleteMapping("/rest/employees/{employeeId}")
    @ApiOperation(value="Delete an employee by id otherwise suitable response")
    public ResponseEntity deleteEmployee(@ApiParam(value = "Employee unique id whom you want to delete",required = true)@PathVariable("employeeId") int employeeId)
    {
       return  empService.deleteEmployee(employeeId);
    }

}


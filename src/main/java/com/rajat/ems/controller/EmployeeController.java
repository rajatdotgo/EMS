package com.rajat.ems.controller;


import com.rajat.ems.entity.Employee;
import com.rajat.ems.exception.BadRequestException;
import com.rajat.ems.exception.ValidationError;
import com.rajat.ems.repository.EmployeeRepo;
import com.rajat.ems.model.PutEmployeeRequestEntity;
import com.rajat.ems.model.PostEmployeeRequestEntity;
import com.rajat.ems.service.EmployeeService;
import com.rajat.ems.service.EmployeeValidate;
import com.rajat.ems.util.MessageConstant;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

@RestController
public class EmployeeController {


    private final EmployeeService employeeService;
    private final EmployeeRepo employeeRepo;
    private final EmployeeValidate employeeValidate;
    private final MessageConstant message;

    @Autowired
    public EmployeeController(EmployeeRepo employeeRepo, EmployeeValidate employeeValidate, MessageConstant message, EmployeeService employeeService) {
        this.employeeRepo = employeeRepo;
        this.employeeValidate = employeeValidate;
        this.message = message;
        this.employeeService = employeeService;
    }


    @GetMapping("/rest/employees")
    @ApiOperation(value = "Finds all the employees sorted according to their designation")
    public ResponseEntity allEmployees() {
        List<Employee> list = employeeService.getAllEmployees();
        return new ResponseEntity<>(list,HttpStatus.OK);
    }

    @GetMapping("/rest/employees/{empId}")
    @ApiOperation(value = "Finds an employee by employee id otherwise suitable response")
    public ResponseEntity getEmployee(@ApiParam(value = "Employee unique id for the details you need to retrieve", example = "1", required = true) @PathVariable("empId") Integer empId) {
        if (empId <= 0) {
            return new ResponseEntity<>(message.getMessage("INVALID_ID"), HttpStatus.BAD_REQUEST);
        }
        Map<String, Object> map = employeeService.findEmployeeById(empId);
        return new ResponseEntity(map,HttpStatus.OK);
    }


    @PostMapping(path = "/rest/employees")
    @ApiOperation(value = "Adds a new employee in the organisation")
    public ResponseEntity saveData(@RequestBody PostEmployeeRequestEntity employee) {
        Employee newEmployee = employeeService.addEmployee(employee);
        return new ResponseEntity(newEmployee,HttpStatus.CREATED);
    }

    @PutMapping("/rest/employees/{empId}")
    @ApiOperation(value = "Updates a particular employee by Id ")
    public ResponseEntity putData(@ApiParam(value = "Employee unique id whose details you need to update", example = "1", required = true) @PathVariable("empId") int empId, @RequestBody PutEmployeeRequestEntity emp) {

        Map<String, Object> map ;

        try{
            employeeValidate.validateId(empId);
        }
        catch (ValidationError error)
        {
            throw new BadRequestException(message.getMessage("VALIDATION_ERROR_INVALID_ID",error.cause));
        }

//        if (empId <= 0) {
//            return new ResponseEntity<>(message.getMessage("INVALID_ID"), HttpStatus.BAD_REQUEST);
//        }
//        if (!employeeValidate.empExist(empId)) {
//            return new ResponseEntity<>(message.getMessage("NO_RECORD_FOUND"), HttpStatus.BAD_REQUEST);
//        }
        employeeValidate.validateBody(emp.getName(),emp.getManagerId(),emp.getJobTitle());
//        if ((StringUtils.isEmpty(emp.getName())) && (emp.getManagerId() == null) && (StringUtils.isEmpty(emp.getJobTitle()))) {
//            return new ResponseEntity<>(message.getMessage("INSUFFICIENT_DATA"), HttpStatus.BAD_REQUEST);
//        }
        if (employeeRepo.findByEmployeeId(empId).designation.getDesignationId() == 1 && (!emp.getJobTitle().equals("Director"))&&(!StringUtils.isEmpty(emp.getJobTitle()))) {
            return new ResponseEntity<>("You can not alter the Director", HttpStatus.BAD_REQUEST);
        }

        // when replace is true
        if (emp.isReplace()) {
            map = employeeService.replaceEmployee(empId, emp);
        }
        // when replace is false
        else {
            map = employeeService.employeeUpdate(empId, emp);
        }
        return new ResponseEntity(map,HttpStatus.OK);
    }

    @DeleteMapping("/rest/employees/{employeeId}")
    @ApiOperation(value = "Delete an employee by id otherwise suitable response")
    public ResponseEntity deleteEmployee(@ApiParam(value = "Employee unique id whom you want to delete", example = "1", required = true) @PathVariable("employeeId") int employeeId) {
        if (employeeId <= 0) {
            return new ResponseEntity<>(message.getMessage("INVALID_ID"), HttpStatus.BAD_REQUEST);
        }
        employeeService.deleteEmployee(employeeId);
        return new ResponseEntity(message.getMessage("DELETED"),HttpStatus.NO_CONTENT);
    }

}


package com.rajat.demoemp1.controller;

import com.rajat.demoemp1.repository.DesignationRepo;
import com.rajat.demoemp1.repository.EmployeeRepo;
import com.rajat.demoemp1.model.Designation;
import com.rajat.demoemp1.model.Employee;
import com.rajat.demoemp1.model.PostRequest;
import com.rajat.demoemp1.service.employeeService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ControllerClass {

    @Autowired
    EmployeeRepo empRepo;
    @Autowired
    DesignationRepo degRepo;
    @Autowired
    employeeService empService;

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

        String empName=employee.getEmpName();
        String desg=employee.getEmpDesg().toUpperCase();
        Integer parent=employee.getParentId();

        Designation designation=degRepo.findByDesgName(desg);
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
    public ResponseEntity putData(@ApiParam(value = "Employee unique id whose details you need to update",required = true)@PathVariable("empId") int empId, @RequestBody PostRequest emp)
    {
        String result="";
        HttpStatus status=null;
        // when replace is true
        if(emp.isReplace())
        {
            Employee emp1 = empRepo.findByEmpId(empId);
            if(emp1==null) result = "employee does not exist";
            else
            {
                Integer parID= emp1.getParentId();
                Employee newEmployee = new Employee();
                newEmployee.setParentId(parID);
                newEmployee.setEmpName(emp.getEmpName());
                if(empRepo.findByEmpId(empId).designation.level>=degRepo.findByDesgName(emp.empDesg.toUpperCase()).level)
                {
                    newEmployee.designation=degRepo.findByDesgName(emp.empDesg.toUpperCase());
                    empRepo.save(newEmployee);
                    List<Employee> parChange =empRepo.findAllByParentId(empId);
                    for(Employee a:parChange)
                    {
                        a.setParentId(newEmployee.getEmpId());
                        empRepo.save(a);
                    }
                    result = "repalced";
                    status=HttpStatus.OK;



                }
                else{
                    result = "Invalid request";
                    status=HttpStatus.BAD_REQUEST;
                }

            }

        }

        // when replace is false
        else
        {
            Employee employee = empRepo.findByEmpId(empId);
            if(employee!=null)
            {
                Integer parentID=emp.getParentId();
                String empDesg=emp.getEmpDesg().toUpperCase();
                String empName=emp.getEmpName();
                Employee elderChild = new Employee();
                if(empRepo.findAllByParentId(empId).size()>0) {
                    elderChild = empRepo.findAllByParentIdOrderByDesignation_levelAsc(empId).get(0);
                }

                if(parentID!=null) {
                    float parLevel =  empRepo.findByEmpId(parentID).designation.getLevel();
                    if (empDesg != null) {
                        float desgLevel = degRepo.findByDesgName(empDesg.toUpperCase()).getLevel();

                        if (parLevel < desgLevel ) {
                            employee.setParentId(parentID);
                            employee.designation= degRepo.findByDesgName(empDesg);
                            //employee.designation.setDesgName(empDesg);
                            empRepo.save(employee);
                            result="Updated";
                            status=HttpStatus.OK;
                        }
                        else
                        {
                            return new ResponseEntity( empDesg+" can not report to "+empRepo.findByEmpId(parentID).getDesgName(),HttpStatus.BAD_REQUEST);
                        }
                    }
                    else
                    {
                        float parentLevel=empRepo.findByEmpId(emp.getParentId()).designation.getLevel();
                        float selfLevel=empRepo.findByEmpId(empId).designation.getLevel();
                        if(parentLevel<selfLevel)
                        {
                            employee.setParentId(emp.getParentId());
                            result = "Updated";
                            status=HttpStatus.OK;
                        }
                        else
                        {
                            result="Bad Request";
                            status=HttpStatus.BAD_REQUEST;
                        }
                    }
                }
                else if(parentID==null) {
                    float parLevel = empRepo.findByEmpId(employee.getParentId()).designation.getLevel();
                    float desgLevel = degRepo.findByDesgName(empDesg).getLevel();
                    if(!(desgLevel<elderChild.designation.getLevel()))
                    {
                        result="bad request";
                        return new ResponseEntity("Supervisor can not be lower by subordinate",HttpStatus.BAD_REQUEST);
                    }
                    if (empName != null&& empDesg!=null) {


                        if ((parLevel < desgLevel)&& desgLevel<elderChild.designation.getLevel()) {
                            employee.setDesgName(empDesg);
                            employee.setEmpName(empName);
                            result="Updated";
                            status=HttpStatus.OK;
                        }

                        else
                        {
                            result="Bad Request";
                            status=HttpStatus.BAD_REQUEST;
                        }
                    }

                    else if(empDesg==null&& empName!=null)
                    {
                        employee.setEmpName(empName);
                        result="Updated";
                        status=HttpStatus.OK;
                    }

                    else if(empDesg!=null&& empName==null)
                    {
                        employee.setDesgName(empDesg);
                        result="Updated";
                        status=HttpStatus.OK;
                    }


                }

                else
                {
                    result="Bad Request";
                }
            }
            else
            {
                result="NO RECORD FOUND";
                status=HttpStatus.NOT_FOUND;
            }

            empRepo.save(employee);

        }
        return new ResponseEntity(result,status);
        }

    @DeleteMapping("/rest/employees/{empId}")
    @ApiOperation(value="Delete an employee by id otherwise suitable response")
    public ResponseEntity deleteEmployee(@ApiParam(value = "Employee unique id whom you want to delete",required = true)@PathVariable("empId") int empId)
    {
        Employee emp=empRepo.findByEmpId(empId);
        if(emp!=null)
        {
            if(emp.getDesgName().equals("DIRECTOR"))
            {
                List<Employee> list=empRepo.findAllByParentId(emp.getEmpId());
                if(list.size()>0)
                {
                    // Not able to delete
                    return new ResponseEntity("Can not delete Director",HttpStatus.BAD_REQUEST);
                }
                else
                {
                    //Able to delete
                    empRepo.delete(emp);
                    return new ResponseEntity("Deleted Successfully",HttpStatus.OK);
                }
            }
            else
            {
                int parentId=emp.getParentId();
                List<Employee> childs=empRepo.findAllByParentId(emp.getEmpId());
                for(Employee employee:childs)
                {
                    employee.setParentId(parentId);
                    empRepo.save(employee);
                }
                empRepo.delete(emp);
                return new ResponseEntity("Deleted Successfully",HttpStatus.OK);
            }
        }
        else
        {
            return new ResponseEntity("Bad Request",HttpStatus.NOT_FOUND);
        }
    }

}


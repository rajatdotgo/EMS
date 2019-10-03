package com.rajat.demoemp1;

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

    @GetMapping("/rest/employees")
    @ApiOperation(value="Finds all the employees sorted according to their designation")
    public ResponseEntity<Object> allEmployee()
    {
        //return empRepo.findAllByOrderByDesignation_levelAscEmpNameAsc();
        Object list=empRepo.findAllByOrderByDesignation_levelAscEmpNameAsc();
        return new ResponseEntity<>(list,HttpStatus.OK);
    }

    @GetMapping("/rest/employees/{empId}")
    @ApiOperation(value="Finds an employee by employee id otherwise suitable response")
    public ResponseEntity findParticular(@ApiParam(value = "Employee unique id for the details you need to retrieve",required = true) @PathVariable("empId") int empId)
    {
        Employee manager=null;
        List<Employee> colleagues=null;
        Map<String,Object> map=new LinkedHashMap<>();
        HttpStatus status = null;
        String res ="";
        Employee emp=empRepo.findByEmpId(empId);
        if(emp==null)
        {
            res = "Employee not found";
            status= HttpStatus.BAD_REQUEST;
            return new ResponseEntity(res,status);
        }
        else {
            map.put("Employee", emp);

            if (emp.getParentId() != null) {
                manager = empRepo.findByEmpId(emp.getParentId());
                map.put("Manager", manager);

                colleagues = empRepo.findAllByParentIdAndEmpIdIsNot(emp.getParentId(), emp.getEmpId());
                map.put("Colleagues", colleagues);
            }

            List<Employee> reporting = empRepo.findAllByParentIdAndEmpIdIsNot(emp.getEmpId(), emp.getEmpId());
            if (reporting.size() != 0)
                map.put("Reporting Too", reporting);

            //res = "Here you go";
            status= HttpStatus.OK;

        }
        return new ResponseEntity<>(map,status);
    }


    @PostMapping(path = "/rest/employees")
    @ApiOperation(value="Adds a new employee in the organisation")
    public ResponseEntity<String> saveData(@RequestBody PostRequest employee)
    {
        HttpStatus status=null;
        String res="";

        String empName=employee.getEmpName();
        String desg=employee.getEmpDesg();
        int parent=employee.getParentId();



        Designation designation=degRepo.findByDesgName(desg);
        float childLevel=designation.getLevel();

        Employee employee1=empRepo.findByEmpId(parent);
        float parLevel=employee1.designation.level;

        if(parLevel<childLevel)
        {
            Employee emp=new Employee(designation,parent,empName);
            empRepo.save(emp);
            status= HttpStatus.OK;
            res="Data Saved";
        }
        else
        {
            status=HttpStatus.BAD_REQUEST;
            res="Bad Request";
        }
        return new ResponseEntity<>(res,status);
    }

    @PutMapping("/rest/employees/{empId}")
    @ApiOperation(value="Updates a particular employee by Id ")
    public ResponseEntity putData(@ApiParam(value = "Employee unique id whose details you need to update",required = true)@PathVariable("empId") int empId, @RequestBody PostRequest emp)
    {
        String result="";
        HttpStatus status=null;
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
                if(empRepo.findByEmpId(empId).designation.level>=degRepo.findByDesgName(emp.empDesg).level)
                {
                    newEmployee.designation=degRepo.findByDesgName(emp.empDesg);
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
        else
        {
            Employee employee = empRepo.findByEmpId(empId);
            if(employee!=null)
            {
                Integer parentID=emp.getParentId();
                String empDesg=emp.getEmpDesg();
                String empName=emp.getEmpName();

                if(parentID!=null) {
                    Employee employee1 = empRepo.findByEmpId(emp.getParentId());
                    float baap = employee1.designation.getLevel();
                    if (empDesg != null) {
                        float desgLevel = degRepo.findByDesgName(emp.getEmpDesg()).getLevel();
                        if (baap < desgLevel) {
                            employee.setParentId(parentID);
                            employee.designation= degRepo.findByDesgName(empDesg);
                            //employee.designation.setDesgName(empDesg);
                            result="Updated";
                            status=HttpStatus.OK;
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
                else if(empDesg!=null && parentID==null) {
                    if (empName != null) {
                        float currParent = empRepo.findByEmpId(employee.getParentId()).designation.getLevel();
                        float desgLevel = degRepo.findByDesgName(empDesg).getLevel();
                        if (!(currParent >= desgLevel)) {
                            employee.setDesgName(empDesg);
                            result="Updated";
                            status=HttpStatus.OK;
                        }
                        else
                        {
                            result="Bad Request";
                            status=HttpStatus.BAD_REQUEST;
                        }
                    }
                }
                else if(empName!=null)
                {
                    employee.setEmpName(empName);
                    result="Updated";
                    status=HttpStatus.OK;
                }
                else
                {
                    result="Bad Request";
                }
            }
            else
            {
                result="Bad Request";
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
            return new ResponseEntity("Bad Request",HttpStatus.BAD_REQUEST);
        }
    }

}


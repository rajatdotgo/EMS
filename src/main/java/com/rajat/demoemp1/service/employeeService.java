package com.rajat.demoemp1.service;

import com.rajat.demoemp1.model.Employee;
import com.rajat.demoemp1.repository.DesignationRepo;
import com.rajat.demoemp1.repository.EmployeeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.rajat.demoemp1.model.putRequest;
import com.rajat.demoemp1.model.Employee;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class employeeService {

    @Autowired
    EmployeeRepo empRepo;
    @Autowired
    DesignationRepo degRepo;
    @Autowired
    employeeValidate empValidate;

   public ResponseEntity getAll()
    {
        List<Employee> list=empRepo.findAllByOrderByDesignation_levelAscEmpNameAsc();
        if(list.size()>0) {
            return new ResponseEntity<>(list, HttpStatus.OK);
        }
        else
        {
            return  new ResponseEntity("No record found",HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity findParticular(int empId){

        Employee manager=null;
        List<Employee> colleagues=null;
        Map<String,Object> map=new LinkedHashMap<>();
        HttpStatus status = null;
        String res ="";
        Employee emp=empRepo.findByEmpId(empId);
        if(!empValidate.empExist(empId))
        {

            return new ResponseEntity("No record found",HttpStatus.NOT_FOUND);
        }
        else {
            map.put("Employee", emp);

            if (emp.getParentId() != null) {
                manager = empRepo.findByEmpId(emp.getParentId());
                map.put("Manager", manager);

                colleagues = empRepo.findAllByParentIdAndEmpIdIsNot(emp.getParentId(), emp.getEmpId());
                if (colleagues.size() != 0)
                     map.put("Colleagues", colleagues);
            }

            List<Employee> reporting = empRepo.findAllByParentIdAndEmpIdIsNot(emp.getEmpId(), emp.getEmpId());
            if (reporting.size() != 0)
                map.put("Reporting Too", reporting);
            status= HttpStatus.OK;

        }
        return new ResponseEntity<>(map,status);

    }

    public void updateSupervisor(Integer oldId,Integer newId)
    {
        List <Employee> subordinates=empRepo.findAllByParentId(oldId);
       if(subordinates.size()>0) {
           for(Employee emp: subordinates)
           {
               emp.setParentId(newId);
               empRepo.save(emp);
           }

        }
    }


    public ResponseEntity deleteEmployee(int id){
        if(!empValidate.empExist(id))
        {
            return new ResponseEntity("No record found",HttpStatus.NOT_FOUND);
        }
        else
        {
                Employee emp=empRepo.findByEmpId(id);
                if(emp.getDesgName().equals("DIRECTOR"))
                {
                    if(empRepo.findAllByParentId(emp.getEmpId()).size()>0)
                    {
                        // Not able to delete as there are some subordinates of director are present
                        return new ResponseEntity("Can not delete Director",HttpStatus.FORBIDDEN);
                    }
                    else
                    {
                        //Able to delete as there is no subordinates of director
                        empRepo.delete(emp);
                        return new ResponseEntity("Deleted Successfully",HttpStatus.OK);
                    }
                }
                else
                {
                    int parentId=emp.getParentId();
                    this.updateSupervisor(id,parentId);
                    empRepo.delete(emp);
                    return new ResponseEntity("Deleted Successfully",HttpStatus.OK);
                }
        }

    }


    public ResponseEntity employeeUpdate(int oldId,putRequest emp)
    {
        Employee employee = empRepo.findByEmpId(oldId);

        if(emp.getEmpName()==null&&emp.getParentId()==null&&emp.getEmpDesg()==null)
        {
            return new ResponseEntity("Please enter some data you wanted to update",HttpStatus.EXPECTATION_FAILED);
        }
        if(emp.getEmpDesg()!=null)
        {
             if (empRepo.findByEmpId(oldId).designation.getDesId() == 1)
                    return new ResponseEntity("You can not alter designation of   Director", HttpStatus.FORBIDDEN);
            if(empValidate.designationChange(employee,emp.getEmpDesg().toUpperCase())){
                employee.setDesgName(emp.getEmpDesg());
            }
            else
                return new ResponseEntity("Invalid Designation entered",HttpStatus.BAD_REQUEST);
        }

        if(emp.getParentId()!=null)
        {
            if (empRepo.findByEmpId(oldId).designation.getDesId() == 1)
                return new ResponseEntity("You can not alter the Director", HttpStatus.FORBIDDEN);
            if(empValidate.parentPossible(employee,emp.getParentId()))
            {
                employee.setParentId(emp.getParentId());
            }
            else
                return new ResponseEntity("Invalid parentId entered",HttpStatus.BAD_REQUEST);
        }

        if(emp.getEmpName()!=null) {
            employee.setEmpName(emp.getEmpName());
        }

        empRepo.save(employee);
        return new ResponseEntity("Employee data successfuly updated",HttpStatus.OK);
    }

    public ResponseEntity replaceEmployee(int empId,putRequest emp)
    {
        if(!empValidate.desExist(emp.getEmpDesg().toUpperCase())) return new ResponseEntity("Designation does not exist please enter a valid one",HttpStatus.BAD_REQUEST);
        else if(empValidate.designationValid(empRepo.findByEmpId(empId),emp.getEmpDesg().toUpperCase()))
        {

           Employee newEmployee=new Employee(degRepo.findByDesgNameLike(emp.getEmpDesg().toUpperCase()),empRepo.findByEmpId(empId).getParentId(),emp.getEmpName());
           empRepo.save(newEmployee);
           this.updateSupervisor(empId,newEmployee.getEmpId());
           empRepo.delete(empRepo.findByEmpId(empId));
           return new ResponseEntity("Employee has been replaced succcessfuly",HttpStatus.OK);
        }
        else return new ResponseEntity("Invalid designation entered",HttpStatus.BAD_REQUEST);
    }
}

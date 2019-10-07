package com.rajat.demoemp1.service;

import com.rajat.demoemp1.model.Employee;
import com.rajat.demoemp1.repository.DesignationRepo;
import com.rajat.demoemp1.repository.EmployeeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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

            //res = "Here you go";
            status= HttpStatus.OK;

        }
        return new ResponseEntity<>(map,status);

    }

public ResponseEntity deleteEmployee(int id){
    if(!empValidate.empExist(id))
    {
        return new ResponseEntity("No record found",HttpStatus.NOT_FOUND);
    }
}
}

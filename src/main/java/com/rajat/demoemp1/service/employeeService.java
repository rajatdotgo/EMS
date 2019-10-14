package com.rajat.demoemp1.service;

import com.rajat.demoemp1.model.Employee;
import com.rajat.demoemp1.repository.DesignationRepo;
import com.rajat.demoemp1.repository.EmployeeRepo;
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
//
//    public Map<String,Object> findUpdated(int empId)
//    {
//        Employee manager=null;
//        List<Employee> colleagues=null;
//        Map<String,Object> map=new LinkedHashMap<>();
//        Employee emp=empRepo.findByEmpId(empId);
//
//        map.put("employee", emp);
//
//        if (emp.getParentId() != null) {
//            manager = empRepo.findByEmpId(emp.getParentId());
//            map.put("manager", manager);
//
//            colleagues = empRepo.findAllByParentIdAndEmpIdIsNotOrderByDesignation_levelAscEmpNameAsc(emp.getParentId(), emp.getEmpId());
//            if (colleagues.size() != 0)
//                map.put("colleagues", colleagues);
//        }
//
//        List<Employee> reporting = empRepo.findAllByParentIdAndEmpIdIsNotOrderByDesignation_levelAscEmpNameAsc(emp.getEmpId(), emp.getEmpId());
//        if (reporting.size() != 0)
//            map.put("subordinates", reporting);
//        return map;
//
//    }

    public ResponseEntity findParticular(int empId){

        Employee manager=null;
        List<Employee> colleagues=null;
        Map<String,Object> map=new LinkedHashMap<>();
        HttpStatus status = null;
        String res ="";
        Employee emp=empRepo.findByEmpId(empId);
        if(!empValidate.empExist(empId))
        {

            return new ResponseEntity("Nooo record found",HttpStatus.NOT_FOUND);
        }

        else {
            map.put("employee", emp);

            if (emp.getParentId() != null) {
                manager = empRepo.findByEmpId(emp.getParentId());
                map.put("manager", manager);

                colleagues = empRepo.findAllByParentIdAndEmpIdIsNotOrderByDesignation_levelAscEmpNameAsc(emp.getParentId(), emp.getEmpId());
                if (colleagues.size() != 0)
                     map.put("colleagues", colleagues);
            }

            List<Employee> reporting = empRepo.findAllByParentIdAndEmpIdIsNotOrderByDesignation_levelAscEmpNameAsc(emp.getEmpId(), emp.getEmpId());
            if (reporting.size() != 0)
                map.put("subordinates", reporting);
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
            return new ResponseEntity<>("No record found",HttpStatus.NOT_FOUND);
        }
        else
        {
                Employee emp=empRepo.findByEmpId(id);
                if(emp.getDesgName().equals("Director"))
                {
                    if(empRepo.findAllByParentId(emp.getEmpId()).size()>0)
                    {
                        // Not able to delete as there are some subordinates of director are present
                        return new ResponseEntity<>("Can not delete Director",HttpStatus.BAD_REQUEST);
                    }
                    else
                    {
                        //Able to delete as there is no subordinates of director
                        empRepo.delete(emp);
                        return new ResponseEntity<>("Deleted Successfully",HttpStatus.NO_CONTENT);
                    }
                }
                else
                {
                    int parentId=emp.getParentId();
                    this.updateSupervisor(id,parentId);
                    empRepo.delete(emp);
                    return new ResponseEntity<>("Deleted Successfully",HttpStatus.NO_CONTENT);
                }
        }

    }


    public ResponseEntity employeeUpdate(int oldId, PutRequest emp)
    {
        Employee employee = empRepo.findByEmpId(oldId);


        if((emp.getName()==null||emp.getName()=="")&&(emp.getManagerId()==null)&&(emp.getJobTitle()==null||emp.getJobTitle()==""))
        {
            return new ResponseEntity<>("Please enter some data you wanted to update",HttpStatus.BAD_REQUEST);
        }


        if(emp.getManagerId()!=null)
        {
            System.out.println(emp.getManagerId());
            if (empRepo.findByEmpId(oldId).designation.getDesId() == 1 )
                return new ResponseEntity("You can not alter the Director", HttpStatus.BAD_REQUEST);
            if(emp.getJobTitle()==null) {
                if (empValidate.parentPossible(employee, emp.getManagerId())) {
                    employee.setParentId(emp.getManagerId());
                    // empRepo.save(employee);
                } else
                    return new ResponseEntity("Invalid parentId entered", HttpStatus.BAD_REQUEST);
            }
            else if(empValidate.desExist(emp.getJobTitle()))
            {
                employee.designation=degRepo.findByDesgNameLike(emp.getJobTitle());
                if (empValidate.parentPossible(employee, emp.getManagerId())) {
                    employee.setParentId(emp.getManagerId());
                     empRepo.save(employee);
                } else
                    return new ResponseEntity("Invalid entry", HttpStatus.BAD_REQUEST);
            }
        }

        if(emp.getJobTitle()!=null&&emp.getJobTitle()!="")
        {
            if (empRepo.findByEmpId(oldId).designation.getDesId() == 1 &&(! emp.getJobTitle().equals("Director"))){
                return new ResponseEntity("You can not alt designation of  Director ", HttpStatus.BAD_REQUEST);
            }
            if(emp.getManagerId()==null) {
                if (empValidate.designationChange(employee, emp.getJobTitle())) {
                    employee.designation = degRepo.findByDesgNameLike(emp.getJobTitle());
                    //  empRepo.save(employee);
                    //System.out.println("raam 2");
                } else
                    return new ResponseEntity("Invalid Designation entered", HttpStatus.BAD_REQUEST);
            }
            else if(empValidate.empExist(emp.getManagerId()))
            {
                employee.setParentId(emp.getManagerId());
                empRepo.save(employee);
                if (empValidate.designationChange(employee, emp.getJobTitle())) {
                    employee.designation = degRepo.findByDesgNameLike(emp.getJobTitle());
                    // empRepo.save(employee);
                    //System.out.println("raam 2");
                } else
                    return new ResponseEntity("Invalid entry", HttpStatus.BAD_REQUEST);
            }
        }

        if(emp.getName()!=null&&(!emp.getName().trim().equals(""))) {
            employee.setEmpName(emp.getName());
           // empRepo.save(employee);
        }

        empRepo.save(employee);
        ResponseEntity response = this.findParticular(employee.getEmpId());
        return new ResponseEntity(response.getBody(),HttpStatus.OK);
    }

    public ResponseEntity replaceEmployee(int empId, PutRequest emp)
    {
        if(!empValidate.desExist(emp.getJobTitle())) return new ResponseEntity("Designation does not exist please enter a valid one",HttpStatus.BAD_REQUEST);
        if(emp.getName()==null||emp.getName().trim().equals("")) return new ResponseEntity("Please enter the name of the employee",HttpStatus.BAD_REQUEST);

        else if(emp.getManagerId()!=null)
        {
            if(!empValidate.empExist(emp.managerId))
            {
                return new ResponseEntity("Invalid parentId entered",HttpStatus.BAD_REQUEST);
            }
            Employee newEmployee=new Employee();
            newEmployee.designation=degRepo.findByDesgNameLike(emp.getJobTitle());
            if(empValidate.parentPossible(newEmployee,emp.getManagerId()))
            {
                newEmployee.setEmpName(emp.getName());
                newEmployee.setParentId(emp.getManagerId());
                empRepo.save(newEmployee);
                this.updateSupervisor(empId,newEmployee.getEmpId());
                empRepo.delete(empRepo.findByEmpId(empId));
                ResponseEntity response = this.findParticular(newEmployee.getEmpId());
                return new ResponseEntity(response.getBody(),HttpStatus.OK);
            }
            else return new ResponseEntity("Invalid parent id entered",HttpStatus.BAD_REQUEST);
        }

        else if(empValidate.designationValid(empRepo.findByEmpId(empId),emp.getJobTitle())&&emp.getManagerId()==null)
        {

           Employee newEmployee=new Employee(degRepo.findByDesgNameLike(emp.getJobTitle()),empRepo.findByEmpId(empId).getParentId(),emp.getName());
           empRepo.save(newEmployee);
           this.updateSupervisor(empId,newEmployee.getEmpId());
           empRepo.delete(empRepo.findByEmpId(empId));
           ResponseEntity response = this.findParticular(newEmployee.getEmpId());
           return new ResponseEntity(response.getBody(),HttpStatus.OK);
        }

        else return new ResponseEntity("Invalid designation entered",HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity addEmployee(PostRequest employee)
    {
        if(!empValidate.desExist(employee.getJobTitle()))
        {
            return new ResponseEntity("Please enter valid designation",HttpStatus.BAD_REQUEST);
        }
       else if(!(empValidate.empExist(employee.getManagerId()))&& degRepo.findByDesgNameLike(employee.getJobTitle()).getDesId()!=1){  //parent null and desg is not director
            return new ResponseEntity("PLease enter a valid supervisor id",HttpStatus.BAD_REQUEST);

        }

       else if(employee.getName()==null||employee.getName().matches(".*\\d.*"))
        {
            return new ResponseEntity("Please enter name of the new employee",HttpStatus.BAD_REQUEST);
        }
        if(!empValidate.empExist(employee.getManagerId()))
        {
            if( empRepo.findAll().size()<=0)
            {
                if(degRepo.findByDesgNameLike(employee.getJobTitle()).getDesId()==1) {
                    Employee emp = new Employee(degRepo.findByDesgNameLike(employee.getJobTitle()), employee.getManagerId(), employee.getName());
                    empRepo.save(emp);

                    return new ResponseEntity<>(emp,HttpStatus.CREATED);
                }
                else
                {
                    return new ResponseEntity<>("Unable to find any DIRECTOR in the organization at the moment",HttpStatus.BAD_REQUEST);
                }
            }
            else
            {
                return new ResponseEntity("Please enter valid supervisor id",HttpStatus.BAD_REQUEST);
            }
        }
        else
        {
            if(empRepo.findByEmpId(employee.getManagerId()).designation.getLevel()<degRepo.findByDesgNameLike(employee.getJobTitle()).getLevel())
            {
                Employee emp = new Employee(degRepo.findByDesgNameLike(employee.getJobTitle()), employee.getManagerId(), employee.getName());
                empRepo.save(emp);

                return new ResponseEntity<>(emp,HttpStatus.CREATED);
            }
            else
            {
                return new ResponseEntity("Please enter valid supervisor id",HttpStatus.BAD_REQUEST);
            }

        }


    }
}

package com.rajat.demoemp1.service;

import com.rajat.demoemp1.model.Employee;
import com.rajat.demoemp1.repository.DesignationRepo;
import com.rajat.demoemp1.repository.EmployeeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmployeeValidate {

    @Autowired
    EmployeeRepo empRepo;
    @Autowired
    DesignationRepo degRepo;


    public Boolean empExist(Integer id){
        if(id==null) return false;
        return(empRepo.findByEmpId(id)!=null);
    }

    public Boolean desExist(String desg)
    {
        if(desg==null||desg.trim().equals("")) return false;
        return (degRepo.findByDesgNameLike(desg)!=null);
    }

    public Boolean isSmallerThanParent(Employee employee, String newDesg){

        return empRepo.findByEmpId(employee.getParentId()).designation.getLevel()<degRepo.findByDesgNameLike(newDesg).getLevel();
    }

    public Boolean isGreaterThanChild(Employee employee, String newDesg){
        float elderChild=99999;
        if(!empRepo.findAllByParentId(employee.getEmpId()).isEmpty()) {
            elderChild = empRepo.findAllByParentIdOrderByDesignation_levelAscEmpNameAsc(employee.getEmpId()).get(0).designation.getLevel();
        }
        return degRepo.findByDesgNameLike(newDesg).getLevel()<elderChild;
    }

    public Boolean designationValid(Employee employee,String newDesg)
    {

        if(employee.designation.getDesId()==1)
        {
            if(employee.designation.getDesId()==degRepo.findByDesgNameLike(newDesg).getDesId())
            {
                return true;
            }
        }
        else if(this.isSmallerThanParent(employee,newDesg)&&this.isGreaterThanChild(employee,newDesg))
        //else if(empRepo.findByEmpId(employee.getParentId()).designation.getLevel()<degRepo.findByDesgNameLike(newDesg).getLevel()&&degRepo.findByDesgNameLike(newDesg).getLevel()<=employee.designation.getLevel())
       {
           return true;
       }

            return false;

    }



    public Boolean designationChange(Employee employee , String newDesg)
    {
        if(!this.desExist(newDesg))
        {
            return false;
        }

        if(empRepo.findByEmpId(employee.getParentId())==null)
        {
            return true;
        }                                                                                                 // comment below is just for checking purpose plz leave as it is
        if(this.isSmallerThanParent(employee,newDesg)&&this.isGreaterThanChild(employee,newDesg) )       //&& degRepo.findByDesgNameLike(newDesg).getLevel()>degRepo.findByDesgNameLike("Director").getLevel())
        {

            return true;
        }
        else
        {
            return false;
        }
    }

    public Boolean parentPossible(Employee employee,int parentId)
    {
        if(!empExist(parentId)) return false;
        return (empRepo.findByEmpId(parentId).designation.getLevel()<employee.designation.getLevel()) ;     //checking for level of employee against supervisor


    }
}

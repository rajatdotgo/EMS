package com.rajat.demoemp1.service;

import com.rajat.demoemp1.model.Employee;
import com.rajat.demoemp1.repository.DesignationRepo;
import com.rajat.demoemp1.repository.EmployeeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class employeeValidate {

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
        return (degRepo.findByDesgNameLike(desg.toUpperCase())!=null);
    }

    public Boolean designationValid(Employee employee,String newDesg)
    {
        float elderChild=99999;
        if(empRepo.findAllByParentId(employee.getEmpId()).size()>0) {
            elderChild = empRepo.findAllByParentIdOrderByDesignation_levelAsc(employee.getEmpId()).get(0).designation.getLevel();
        }

        if(employee.designation.getDesId()==1)
        {
            if(employee.designation.getDesId()==degRepo.findByDesgNameLike(newDesg).getDesId())
            {
                return true;
            }
        }
        else if(empRepo.findByEmpId(employee.getParentId()).designation.getLevel()<degRepo.findByDesgNameLike(newDesg).getLevel()&&degRepo.findByDesgNameLike(newDesg).getLevel()<elderChild)
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
        float elderChild = 99999;
        if(empRepo.findByEmpId(employee.getParentId())==null)
        {
            return true;
        }
        if(empRepo.findAllByParentId(employee.getEmpId()).size()>0) {
            elderChild = empRepo.findAllByParentIdOrderByDesignation_levelAsc(employee.getEmpId()).get(0).designation.getLevel();
        }
        if(empRepo.findByEmpId(employee.getParentId()).designation.getLevel()<degRepo.findByDesgNameLike(newDesg).getLevel()&&degRepo.findByDesgNameLike(newDesg).getLevel()<elderChild && degRepo.findByDesgNameLike(newDesg).getLevel()>degRepo.findByDesgNameLike("DIRECTOR").getLevel())
        {
            //System.out.println("RAAAM"+employee.getParentId());
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
        if(empRepo.findByEmpId(parentId).designation.getLevel()<employee.designation.getLevel()) return true;
        else return false;

    }
}

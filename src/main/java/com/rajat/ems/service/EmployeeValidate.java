package com.rajat.ems.service;

import com.rajat.ems.model.Employee;
import com.rajat.ems.repository.DesignationRepo;
import com.rajat.ems.repository.EmployeeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class EmployeeValidate {

    private EmployeeRepo employeeRepo;
    private DesignationRepo designationRepo;

    @Autowired
    public EmployeeValidate(EmployeeRepo employeeRepo, DesignationRepo designationRepo)
    {
        this.employeeRepo = employeeRepo;
        this.designationRepo = designationRepo;

    }


    public boolean empExist(Integer id){
        if(id==null) return false;
        return(employeeRepo.findByEmployeeId(id)!=null);


    }

     boolean desExist(String desg)
    {
        if(desg==null||desg.trim().equals("")) return false;
        return (designationRepo.findByDesignationNameLike(desg)!=null);
    }

    private boolean isSmallerThanParent(Employee employee, String newDesg){

        return employeeRepo.findByEmployeeId(employee.getParentId()).designation.getLevel()< designationRepo.findByDesignationNameLike(newDesg).getLevel();
    }

    private boolean isGreaterThanChild(Employee employee, String newDesg){
        float elderChild=99999;
        if(!employeeRepo.findAllByParentId(employee.getEmployeeId()).isEmpty()) {
            elderChild = employeeRepo.findAllByParentIdOrderByDesignation_levelAscEmployeeNameAsc(employee.getEmployeeId()).get(0).designation.getLevel();
        }
        return designationRepo.findByDesignationNameLike(newDesg).getLevel()<elderChild;
    }

    boolean designationValid(Employee employee, String newDesg)
    {

        if(employee.designation.getDesignationId()==1)
        {
            if(employee.designation.getDesignationId().equals(designationRepo.findByDesignationNameLike(newDesg).getDesignationId()))
            {
                return true;
            }
        }
        else return this.isSmallerThanParent(employee, newDesg) && this.isGreaterThanChild(employee, newDesg);

            return false;

    }



    boolean designationChange(Employee employee, String newDesg)
    {
        if(!this.desExist(newDesg))
        {
            return false;
        }

        if(employeeRepo.findByEmployeeId(employee.getParentId())==null)
        {
            return true;
        }
        return (this.isSmallerThanParent(employee,newDesg)&&this.isGreaterThanChild(employee,newDesg) );

    }

    boolean parentPossible(Employee employee, int parentId)
    {
        if(!empExist(parentId)) return false;
        return (employeeRepo.findByEmployeeId(parentId).designation.getLevel()<employee.designation.getLevel()) ;     //checking for level of employee against supervisor


    }
}

package com.rajat.ems.service;

import com.rajat.ems.entity.Employee;
import com.rajat.ems.exception.BadRequestException;
import com.rajat.ems.exception.ValidationError;
import com.rajat.ems.repository.DesignationRepo;
import com.rajat.ems.repository.EmployeeRepo;
import com.rajat.ems.util.MessageConstant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;




@Service
public class EmployeeValidate {

    private final EmployeeRepo employeeRepo;
    private final DesignationRepo designationRepo;
    private final MessageConstant message;

    @Autowired
    public EmployeeValidate(EmployeeRepo employeeRepo, DesignationRepo designationRepo, MessageConstant message)
    {
        this.employeeRepo = employeeRepo;
        this.designationRepo = designationRepo;
        this.message=message;

    }

    public void validateName(String name,boolean checkEmpty)
    {
        if(checkEmpty) {
            if (StringUtils.isEmpty(name)) {
                throw new ValidationError("name", "empty");
                //return false;
            }
        }

        if (name.matches(".*\\d.*")){
            throw  new ValidationError("name","containing invalid character");
           // return false;
        }

        //return true;

    }
    public void validateId(Integer id)
    {
        if(id==null)
            throw new ValidationError("ID","is null");
        if(id<=0)
            throw  new ValidationError("ID","is invalid");
        if(employeeRepo.findByEmployeeId(id)==null)
            throw  new ValidationError("ID","does not exist");
    }

    public void validateBody(String employeeName, Integer employeeId, String jobTitle)
    {
        if((StringUtils.isEmpty(employeeName)) && (employeeId == null) && (StringUtils.isEmpty(jobTitle)))
        {
            throw new BadRequestException(message.getMessage("INSUFFICIENT_DATA"));
        }
    }

    public boolean empExist(Integer id){
        if(id==null) return false;
        return(employeeRepo.findByEmployeeId(id)!=null);

    }

    boolean designationExist(String designation)
    {
        if(designation==null||designation.trim().equals(""))
            return false;
        return (designationRepo.findByDesignationNameLike(designation)!=null);

    }

     void validateDesignation(String designation)
    {
        if(designation==null||designation.trim().equals(""))
            //return false;
            throw new BadRequestException(message.getMessage("VALIDATION_ERROR_INVALID_DESIGNATION","is empty"));
        else if (designationRepo.findByDesignationNameLike(designation)==null)
            throw new BadRequestException(message.getMessage("VALIDATION_ERROR_INVALID_DESIGNATION","is incorrect"));
    }

    private boolean isSmallerThanParent(Employee employee, String newDesignation){

        return employeeRepo.findByEmployeeId(employee.getParentId()).designation.getLevel()< designationRepo.findByDesignationNameLike(newDesignation).getLevel();
    }

    private boolean isGreaterThanChild(Employee employee, String newDesignation){
        float elderChild=99999;
        if(!employeeRepo.findAllByParentId(employee.getEmployeeId()).isEmpty()) {
            elderChild = employeeRepo.findAllByParentIdOrderByDesignation_levelAscEmployeeNameAsc(employee.getEmployeeId()).get(0).designation.getLevel();
        }
        return designationRepo.findByDesignationNameLike(newDesignation).getLevel()<elderChild;
    }

    boolean designationValid(Employee employee, String newDesg)
    {

        if(employee.designation.getDesignationId()==1)
        {
            return employee.designation.getDesignationId().equals(designationRepo.findByDesignationNameLike(newDesg).getDesignationId());
        }
        else return this.isSmallerThanParent(employee, newDesg) && this.isGreaterThanChild(employee, newDesg);

    }



    boolean designationChange(Employee employee, String newDesignation)
    {
        this.validateDesignation(newDesignation);


            if(employeeRepo.findByEmployeeId(employee.getParentId())==null)
        {
            return true;
        }
        return (this.isSmallerThanParent(employee,newDesignation)&&this.isGreaterThanChild(employee,newDesignation) );

    }

    boolean parentPossible(Employee employee, int parentId)
    {
        if(!empExist(parentId)) return false;
        return (employeeRepo.findByEmployeeId(parentId).designation.getLevel()<employee.designation.getLevel()) ;     //checking for level of employee against supervisor


    }
}

package com.rajat.ems.service;

import com.rajat.ems.entity.Designation;
import com.rajat.ems.entity.Employee;
import com.rajat.ems.exception.BadRequestException;
import com.rajat.ems.exception.NotFoundException;
import com.rajat.ems.model.PostDesignationRequestBody;
import com.rajat.ems.repository.DesignationRepo;
import com.rajat.ems.repository.EmployeeRepo;
import com.rajat.ems.util.MessageConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;


import java.util.List;


@Service
public class DesignationService {
    private final DesignationRepo designationRepo;
    private final MessageConstant message;
    private final EmployeeRepo employeeRepo;
    private final DesignationValidate designationValidate;

    @Autowired
    DesignationService(DesignationValidate designationValidate, DesignationRepo designationRepo,MessageConstant message,EmployeeRepo employeeRepo)
    {
        this.designationRepo=designationRepo;
        this.message=message;
        this.designationValidate = designationValidate;
        this.employeeRepo = employeeRepo;
    }


    /** this will return list of all employees in a sorted order **/
    public List<Designation> getAllDesignation() {
        List<Designation> list = designationRepo.findAllByOrderByLevelAscDesignationNameAsc();
        if (!list.isEmpty()) {
            return list;
        } else {
            throw new NotFoundException(message.getMessage("NO_RECORD_FOUND"));

        }
    }

    public void deleteDesignation(String designationName){
        Designation designationToDelete = designationRepo.findByDesignationNameLike(designationName);
        if(designationToDelete==null)
        {
            throw new BadRequestException(message.getMessage("VALIDATION_ERROR_INVALID_DESIGNATION","does not exist"));
        }
        List<Employee> employees = employeeRepo.findEmployeeByDesignation_DesignationName(designationName);
        if(employees.size()>=1)
        {
            throw new BadRequestException(message.getMessage("ERROR_UNABLE_TO_DELETE_DESIGNATION"));
        }
        designationRepo.delete(designationRepo.findByDesignationNameLike(designationName));
    }

    public void addDesignation(PostDesignationRequestBody postDesignationRequestBody)
    {
        addDesignation(postDesignationRequestBody.getDesignationName(),postDesignationRequestBody.getSenior(),postDesignationRequestBody.isPostEquivalentToSenior());
    }

    private float getNewLevel(String seniorDesignation)
    {


        List<Designation> designations = designationRepo.findAllByOrderByLevelAscDesignationNameAsc();
        Designation senior = designationRepo.findByDesignationNameLike(seniorDesignation);
        Designation designation = designationRepo.findFirstByLevelGreaterThanOrderByLevel(senior.getLevel());

        boolean findNext = false;

        int seniorIndex = designations.indexOf(senior);


        if(designation!=null)
        {
            findNext=true;
        }

        return findNext ? ((designations.get(seniorIndex+1).getLevel() + senior.getLevel()) / 2) : senior.getLevel() + 10f;
    }

    private void addDesignation(String designationName,String seniorDesignation,boolean isPostEquivalentToSenior) {
        Designation newDesignation = new Designation();

        if (designationValidate.desinationExist(designationName)){
            throw new BadRequestException(message.getMessage("ERROR_DESIGNATION_EXIST"));
        }

        if (!designationValidate.desinationExist(seniorDesignation)) {

            if (StringUtils.isEmpty(seniorDesignation)) {
                newDesignation.setDesignationName(designationName);
                newDesignation.setLevel((designationValidate.getHighestDesignation().getLevel())/2);
                designationRepo.save(newDesignation);
            } else
                throw new BadRequestException(message.getMessage("ERROR_INVALID_SENIOR_DESIGNATION"));
        }
        else {
            if (isPostEquivalentToSenior) {
                newDesignation.setDesignationName(designationName);
                newDesignation.setLevel(designationRepo.findByDesignationNameLike(seniorDesignation).level);
                designationRepo.save(newDesignation);
            } else {


                float newLevel = this.getNewLevel(seniorDesignation);

                newDesignation.setDesignationName(designationName);
                newDesignation.setLevel(newLevel);
                designationRepo.save(newDesignation);

            }
        }
    }


}

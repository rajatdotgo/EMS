package com.rajat.ems.service;

import com.rajat.ems.entity.Designation;
import com.rajat.ems.repository.DesignationRepo;
import org.springframework.stereotype.Service;

@Service
public class DesignationValidate {

    private final DesignationRepo designationRepo;

    DesignationValidate(DesignationRepo designationRepo)
    {
        this.designationRepo=designationRepo;
    }
    public boolean desinationExist(String designationName){
        Designation designation = designationRepo.findByDesignationNameLike(designationName);
        return (designation!=null);
    }
    public Designation getHighestDesignation(){
        return designationRepo.findFirstByOrderByLevel();
    }
}

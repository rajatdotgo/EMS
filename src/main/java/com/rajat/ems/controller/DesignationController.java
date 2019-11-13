package com.rajat.ems.controller;


import com.rajat.ems.repository.DesignationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DesignationController {
    private DesignationRepo designationRepo;

    @Autowired
    DesignationController(DesignationRepo designationRepo){
        this.designationRepo=designationRepo;
    }
}

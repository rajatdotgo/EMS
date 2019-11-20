package com.rajat.ems.model;

import com.rajat.ems.entity.Designation;
import com.rajat.ems.exception.BadRequestException;
import com.rajat.ems.repository.DesignationRepo;

import com.rajat.ems.util.MessageConstant;

import java.util.List;

public class PostDesignationRequestBody {
    private String designationName = "";
    private String senior = "";
    private boolean postEquivalentToSenior = false;

    private final MessageConstant message;
    private final DesignationRepo designationRepo;

    PostDesignationRequestBody( MessageConstant message, DesignationRepo designationRepo) {

        this.message = message;
        this.designationRepo = designationRepo;
    }

    public String getDesignationName() {
        return designationName;
    }

    public void setDesignationName(String designationName) {
        this.designationName = designationName;
    }

    public String getSenior() {
        return senior;
    }

    public void setSenior(String senior) {
        this.senior = senior;
    }

    public boolean isPostEquivalentToSenior() {
        return postEquivalentToSenior;
    }

    public void setPostEquivalentToSenior(boolean postEquivalentToSenior) {
        this.postEquivalentToSenior = postEquivalentToSenior;
    }


}

package com.rajat.demoemp1.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


public class PostRequest {


    public String empName;
    public String empDesg;
    public Integer parentId=null;
    public boolean replace=false;

    public boolean isReplace() {
        return replace;
    }

    public void setReplace(boolean replace) {
        this.replace = replace;
    }

    public String getEmpName()
    {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName;
    }

    public String getEmpDesg() {
        return empDesg;
    }

    public void setEmpDesg(String empDesg) {
        this.empDesg = empDesg;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }


        }

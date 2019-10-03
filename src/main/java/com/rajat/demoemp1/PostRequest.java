package com.rajat.demoemp1;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


public class PostRequest {

    int empId=-1;
    String empName;
    String empDesg;
    Integer parentId;
    boolean replace=false;

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

    public int getEmpId() {
        return empId;
    }

    public void setEmpId(int empId) {
        this.empId = empId;
    }
        }

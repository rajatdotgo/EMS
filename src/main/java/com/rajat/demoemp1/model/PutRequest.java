package com.rajat.demoemp1.model;


public class PutRequest {


    public String name =null;
    public String jobTitle =null;
    public Integer managerId =null;
    public boolean replace=false;

    public PutRequest(){}

    public PutRequest(String jobTitle, boolean replace) {
        this.jobTitle = jobTitle;
        this.replace = replace;
    }

    public PutRequest(String name, String jobTitle, Integer managerId, boolean replace) {
        this.name = name.trim();
        this.jobTitle = jobTitle;
        this.managerId = managerId;
        this.replace = replace;
    }

    public boolean isReplace() {
        return replace;
    }

    public void setReplace(boolean replace) {
        this.replace = replace;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public Integer getManagerId() {
        return managerId;
    }

    public void setManagerId(Integer managerId) {
        this.managerId = managerId;
    }

        }

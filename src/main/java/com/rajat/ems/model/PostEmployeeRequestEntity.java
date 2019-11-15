package com.rajat.ems.model;

public class PostEmployeeRequestEntity {


    private String name ="";
    private String jobTitle ="";
    private Integer managerId =null;

   public PostEmployeeRequestEntity()
    {}

    public PostEmployeeRequestEntity(String name, String jobTitle, Integer managerId)
    {
        this.name = name;
        this.jobTitle = jobTitle;
        this.managerId = managerId;
    }

    public PostEmployeeRequestEntity(String name, Integer managerId) {
        this.name = name;
        this.managerId = managerId;
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

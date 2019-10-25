package com.rajat.demoemp1.model;

public class PostRequest {


    private String name ="sdsfwe";
    private String jobTitle ="adsad";
    private Integer managerId =null;

   public PostRequest()
    {}

    public PostRequest(String name, String jobTitle, Integer managerId)
    {
        this.name = name;
        this.jobTitle = jobTitle;
        this.managerId = managerId;
    }

    public PostRequest(String name, Integer managerId) {
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

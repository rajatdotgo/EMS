package com.rajat.ems.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

import javax.persistence.*;

@ApiModel(description = "Details about the employees")
@Entity
public class Employee {
 

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    Integer employeeId;

    @Transient
    String designationName;

    @OneToOne
    @JoinColumn
    @JsonIgnore
   public  Designation designation;

    @JsonProperty("jobTitle")
    public String getDesignationName() {
        return designation.designationName;
    }

    public void setDesignationName(String designationName) {
        this.designationName = designationName;
    }


    @Column(nullable = true)
    @JsonProperty("managerId")
    private Integer parentId;

    @JsonProperty("name")
    String employeeName;


    public   Employee()
    {

    }
    @Override
    public String toString() {
        return "Employee{" +
                "empId=" + employeeId +
                ", designation=" + designation +
                ", parentId=" + parentId +
                ", empName='" + employeeName + '\'' +
                '}';
    }

  public   Employee(Designation designation,Integer parentId,String employeeName)
    {

        this.designation=designation;
        this.parentId=parentId;
        this.employeeName = employeeName;
    }

    public Integer getEmployeeId()
    {
        return employeeId;
    }

    public Designation getDesignation()
    {
        return designation;
    }

    public void setDesignation(Designation designation) {
        this.designation = designation;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName.trim();
    }

    
}

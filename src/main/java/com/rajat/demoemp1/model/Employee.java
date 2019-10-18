package com.rajat.demoemp1.model;

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
    Integer empId;

    @Transient
    String desgName;

    @OneToOne
    @JoinColumn
    @JsonIgnore
   public  Designation designation;

    @JsonProperty("jobTitle")
    public String getDesgName() {
        return designation.desgName;
    }

    public void setDesgName(String desgName) {
        this.desgName = desgName;
    }


    @Column(nullable = true)
    @JsonProperty("managerId")
    private Integer parentId;

    @JsonProperty("name")
    String empName;


    public   Employee()
    {

    }
    @Override
    public String toString() {
        return "Employee{" +
                "empId=" + empId +
                ", designation=" + designation +
                ", parentId=" + parentId +
                ", empName='" + empName + '\'' +
                '}';
    }

  public   Employee(Designation designation,Integer parentId,String empName)
    {

        this.designation=designation;
        this.parentId=parentId;
        this.empName=empName;
    }

    public Integer getEmpId()
    {
        return empId;
    }

    public Designation getDesignation()
    {
        return designation;
    }

    public void setDesignation(Designation designation) {
        this.designation = designation;
    }

    public void setEmpId(Integer empId) {
        this.empId = empId;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public String getEmpName() {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName.trim();
    }

    
}

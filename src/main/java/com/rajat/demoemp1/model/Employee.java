package com.rajat.demoemp1.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rajat.demoemp1.model.Designation;
import io.swagger.annotations.ApiModel;

import javax.persistence.*;

@ApiModel(description = "Details about the employees")
@Entity
public class Employee {
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int empId;

    @Transient
    String desgName;

    @OneToOne
    @JoinColumn
    @JsonIgnore
   public  Designation designation;

    public String getDesgName() {
        return designation.desgName;
    }

    public void setDesgName(String desgName) {
        this.desgName = desgName;
    }

    @JsonIgnore
    @Column(nullable = true)
    private Integer parentId;

    String empName;

  public   Employee(Designation designation,Integer parentId,String empName)
    {
//        this.empId=empId;
        this.designation=designation;
        this.parentId=parentId;
        this.empName=empName;
    }

    public int getEmpId()
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

    public void setEmpId(int empId) {
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
        this.empName = empName;
    }
}

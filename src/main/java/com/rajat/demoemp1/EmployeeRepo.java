package com.rajat.demoemp1;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeRepo extends JpaRepository <Employee,Integer>{

    public List<Employee> findAllByOrderByDesignation_levelAscEmpNameAsc();
    public Employee findByEmpId(int id);
    public List<Employee> findAllByParentIdAndEmpIdIsNot(int parentId,int empId);
    public List<Employee> findAllByParentId(int parId);

}

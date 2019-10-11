package com.rajat.demoemp1.repository;

import com.rajat.demoemp1.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeRepo extends JpaRepository <Employee,Integer>{

    public List<Employee> findAllByOrderByDesignation_levelAscEmpNameAsc();
    public Employee findByEmpId(Integer id);
    public List<Employee> findAllByParentIdAndEmpIdIsNotOrderByDesignation_levelAscEmpNameAsc(int parentId,int empId);
    public List<Employee> findAllByParentId(int parId);
    public List<Employee> findAllByParentIdOrderByDesignation_levelAscEmpNameAsc(int id);
    Employee findByParentId(Integer id);

}

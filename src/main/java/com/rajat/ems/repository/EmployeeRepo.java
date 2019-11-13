package com.rajat.ems.repository;

import com.rajat.ems.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeRepo extends JpaRepository <Employee,Integer>{

    public List<Employee> findAllByOrderByDesignation_levelAscEmployeeNameAsc();
    public Employee findByEmployeeId(Integer id);
    public List<Employee> findAllByParentIdAndEmployeeIdIsNotOrderByDesignation_levelAscEmployeeNameAsc(int parentId, int empId);
    public List<Employee> findAllByParentId(int parId);
    public List<Employee> findAllByParentIdOrderByDesignation_levelAscEmployeeNameAsc(int id);
    Employee findByParentId(Integer id);

}

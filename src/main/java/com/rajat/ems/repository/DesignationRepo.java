package com.rajat.ems.repository;

import com.rajat.ems.model.Designation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DesignationRepo extends JpaRepository<Designation,Integer> {

    public Designation findByDesignationNameLike(String desName);

}

package com.rajat.ems.repository;

import com.rajat.ems.entity.Designation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DesignationRepo extends JpaRepository<Designation,Integer> {

    public Designation findByDesignationNameLike(String desName);
    public List<Designation> findAllByOrderByLevelAscDesignationNameAsc();
    public Designation findFirstByLevelGreaterThanOrderByLevel(float level);
    public Designation findFirstByOrderByLevel();

}

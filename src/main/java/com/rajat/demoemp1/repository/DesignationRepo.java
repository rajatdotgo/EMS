package com.rajat.demoemp1.repository;

import com.rajat.demoemp1.model.Designation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DesignationRepo extends JpaRepository<Designation,Integer> {

    public Designation findByDesgNameLike(String desName);

}

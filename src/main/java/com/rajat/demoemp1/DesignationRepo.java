package com.rajat.demoemp1;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DesignationRepo extends JpaRepository<Designation,Integer> {

    public Designation findByDesgName(String desName);

}

package com.rajat.demoemp1.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
@Entity
public class Designation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    int desId;
    String desgName;
    @JsonIgnore
   public  float level;

    public int getDesId()
    {
        return desId;
    }

    public void setDesId(int desId) {
        this.desId = desId;
    }

    public String getDesgName() {
        return desgName;
    }

    public void setDesgName(String desgName) {
        this.desgName = desgName;
    }

    public float getLevel() {
        return level;
    }

    public void setLevel(float level) {
        this.level = level;
    }
}

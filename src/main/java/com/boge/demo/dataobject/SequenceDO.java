package com.boge.demo.dataobject;

import java.util.Date;

public class SequenceDO {
    private String name;

    private Integer currentvalue;

    private Integer step;

    private Date currentdate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public Integer getCurrentvalue() {
        return currentvalue;
    }

    public void setCurrentvalue(Integer currentvalue) {
        this.currentvalue = currentvalue;
    }

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }

    public Date getCurrentdate() {
        return currentdate;
    }

    public void setCurrentdate(Date currentdate) {
        this.currentdate = currentdate;
    }
}
package com.company.jifiit.model;

public class LeaveModel {
//    private String id;
    private String 	dayName;
    private String date;
    private String dayQuantitis;
    private String approval,toTime,fromTime,leave_type;

    public LeaveModel(String dayName, String date, String dayQuantitis, String approval, String toTime, String fromTime, String leave_type) {
        this.dayName = dayName;
        this.date = date;
        this.dayQuantitis = dayQuantitis;
        this.approval = approval;
        this.toTime = toTime;
        this.fromTime = fromTime;
        this.leave_type = leave_type;
    }

    public String getDayName() {
        return dayName;
    }

    public String getDate() {
        return date;
    }

    public String getDayQuantitis() {
        return dayQuantitis;
    }

    public String getApproval() {
        return approval;
    }

    public String getToTime() {
        return toTime;
    }

    public String getFromTime() {
        return fromTime;
    }

    public String getLeave_type() {
        return leave_type;
    }
}

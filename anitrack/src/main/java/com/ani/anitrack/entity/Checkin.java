package com.ani.anitrack.entity;

public class Checkin {
    private Integer id;
    private Integer userId;
    private String checkDate;
    private Integer count;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getCheckDate() { return checkDate; }
    public void setCheckDate(String checkDate) { this.checkDate = checkDate; }
    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
}
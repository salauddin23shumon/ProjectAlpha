package me.ictlinkbd.com.projectalpha.tourEvent.eventDataModel;

import java.io.Serializable;
import java.util.Map;

public class Users implements Serializable {

    private String userId;
    private String userName;
    private String userEmail;
    private String phoneNumber;
    private String userImageUrl;
    private Map<String, Event> events;


    public Users() {
    }

    public Users(String userId, String userName, String userEmail, String phoneNumber, String userImageUrl, Map<String, Event> events) {
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.phoneNumber = phoneNumber;
        this.userImageUrl = userImageUrl;
        this.events = events;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUserImageUrl() {
        return userImageUrl;
    }

    public void setUserImageUrl(String userImageUrl) {
        this.userImageUrl = userImageUrl;
    }

    public Map<String, Event> getEvents() {
        return events;
    }

    public void setEvents(Map<String, Event> events) {
        this.events = events;
    }
}

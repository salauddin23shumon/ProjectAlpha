package me.ictlinkbd.com.projectalpha.tourEvent.eventDataModel;

import java.io.Serializable;
import java.util.Map;

public class Event implements Serializable {

    private String eventId;
    private String name;
    private String source;
    private String destination;
    private String departureDate;
    private double budget;
    private Map<String, Images> images;
    private Map<String, Expense> expense;

    public Event(String eventId, String name, String source, String destination, String departureDate,
                 double budget, Map<String, Images> images, Map<String, Expense> expense) {
        this.eventId = eventId;
        this.name = name;
        this.source = source;
        this.destination = destination;
        this.departureDate = departureDate;
        this.budget = budget;
        this.images = images;
        this.expense = expense;
    }

    public Event() {

    }


    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(String departureDate) {
        this.departureDate = departureDate;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public Map<String, Images> getImages() {
        return images;
    }

    public void setImages(Map<String, Images> images) {
        this.images = images;
    }

    public Map<String, Expense> getExpense() {
        return expense;
    }

    public void setExpense(Map<String, Expense> expense) {
        this.expense = expense;
    }
}

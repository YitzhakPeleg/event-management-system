package com.eventmanagement.model;

/**
 * Represents an event as received from the API (parsed from JSON).
 * Same structure as the backend model, but used on the Android side.
 */
public class Event {

    private int    id;
    private String title;
    private String eventDate;
    private String eventTime;
    private String hallName;
    private int    hallCapacity;
    private int    registrationCount;

    public Event() {}

    // Getters and setters

    public int    getId()                   { return id; }
    public void   setId(int id)             { this.id = id; }

    public String getTitle()                { return title; }
    public void   setTitle(String title)    { this.title = title; }

    public String getEventDate()                 { return eventDate; }
    public void   setEventDate(String eventDate) { this.eventDate = eventDate; }

    public String getEventTime()                 { return eventTime; }
    public void   setEventTime(String eventTime) { this.eventTime = eventTime; }

    public String getHallName()                  { return hallName; }
    public void   setHallName(String hallName)   { this.hallName = hallName; }

    public int  getHallCapacity()                     { return hallCapacity; }
    public void setHallCapacity(int hallCapacity)     { this.hallCapacity = hallCapacity; }

    public int  getRegistrationCount()                        { return registrationCount; }
    public void setRegistrationCount(int registrationCount)   { this.registrationCount = registrationCount; }
}

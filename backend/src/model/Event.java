package model;

/**
 * Represents a conference or event.
 * Each event is held in a specific hall and was created by an admin user.
 */
public class Event {

    // ---- Fields ----
    private int id;
    private String title;
    private String description;
    private String eventDate;   // stored as "YYYY-MM-DD"
    private String eventTime;   // stored as "HH:MM:SS"
    private int hallId;
    private int createdBy;      // user id of the admin who created this event

    // Extra fields populated by JOIN queries (not stored in this table)
    private String hallName;    // from halls table
    private int hallCapacity;   // from halls table
    private int registrationCount; // from event_registrations count

    // ---- Constructors ----

    public Event() {}

    public Event(int id, String title, String description,
                 String eventDate, String eventTime,
                 int hallId, int createdBy) {
        this.id          = id;
        this.title       = title;
        this.description = description;
        this.eventDate   = eventDate;
        this.eventTime   = eventTime;
        this.hallId      = hallId;
        this.createdBy   = createdBy;
    }

    // ---- Getters and Setters ----

    public int getId()               { return id; }
    public void setId(int id)        { this.id = id; }

    public String getTitle()              { return title; }
    public void setTitle(String title)    { this.title = title; }

    public String getDescription()                  { return description; }
    public void setDescription(String description)  { this.description = description; }

    public String getEventDate()                 { return eventDate; }
    public void setEventDate(String eventDate)   { this.eventDate = eventDate; }

    public String getEventTime()                 { return eventTime; }
    public void setEventTime(String eventTime)   { this.eventTime = eventTime; }

    public int getHallId()               { return hallId; }
    public void setHallId(int hallId)    { this.hallId = hallId; }

    public int getCreatedBy()                { return createdBy; }
    public void setCreatedBy(int createdBy)  { this.createdBy = createdBy; }

    public String getHallName()                  { return hallName; }
    public void setHallName(String hallName)     { this.hallName = hallName; }

    public int getHallCapacity()                     { return hallCapacity; }
    public void setHallCapacity(int hallCapacity)    { this.hallCapacity = hallCapacity; }

    public int getRegistrationCount()                        { return registrationCount; }
    public void setRegistrationCount(int registrationCount)  { this.registrationCount = registrationCount; }

    /** Returns true if the event still has open spots. */
    public boolean hasAvailableSpots() {
        return registrationCount < hallCapacity;
    }

    @Override
    public String toString() {
        return "Event{id=" + id + ", title=" + title + ", date=" + eventDate + ", hallId=" + hallId + "}";
    }
}

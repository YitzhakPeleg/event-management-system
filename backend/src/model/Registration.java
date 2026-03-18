package model;

/**
 * Represents a registration of a participant to an event.
 * This is the linking (junction) table between events and participants.
 * qrCodeToken is a unique random string used to generate and scan QR codes at the entrance.
 */
public class Registration {

    // ---- Fields ----
    private int id;
    private int eventId;
    private int participantId;
    private String registeredAt;   // "YYYY-MM-DD HH:MM:SS"
    private String qrCodeToken;    // unique random string for QR code

    // Extra fields populated by JOIN queries (not stored in this table)
    private String eventTitle;
    private String eventDate;
    private String participantName;

    // ---- Constructors ----

    public Registration() {}

    public Registration(int id, int eventId, int participantId,
                        String registeredAt, String qrCodeToken) {
        this.id            = id;
        this.eventId       = eventId;
        this.participantId = participantId;
        this.registeredAt  = registeredAt;
        this.qrCodeToken   = qrCodeToken;
    }

    // ---- Getters and Setters ----

    public int getId()               { return id; }
    public void setId(int id)        { this.id = id; }

    public int getEventId()                  { return eventId; }
    public void setEventId(int eventId)      { this.eventId = eventId; }

    public int getParticipantId()                    { return participantId; }
    public void setParticipantId(int participantId)  { this.participantId = participantId; }

    public String getRegisteredAt()                    { return registeredAt; }
    public void setRegisteredAt(String registeredAt)   { this.registeredAt = registeredAt; }

    public String getQrCodeToken()                   { return qrCodeToken; }
    public void setQrCodeToken(String qrCodeToken)   { this.qrCodeToken = qrCodeToken; }

    public String getEventTitle()                  { return eventTitle; }
    public void setEventTitle(String eventTitle)   { this.eventTitle = eventTitle; }

    public String getEventDate()                 { return eventDate; }
    public void setEventDate(String eventDate)   { this.eventDate = eventDate; }

    public String getParticipantName()                     { return participantName; }
    public void setParticipantName(String participantName) { this.participantName = participantName; }

    @Override
    public String toString() {
        return "Registration{id=" + id + ", eventId=" + eventId
                + ", participantId=" + participantId + ", token=" + qrCodeToken + "}";
    }
}

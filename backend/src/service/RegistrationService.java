package service;

import db.EventDAO;
import db.ParticipantDAO;
import db.RegistrationDAO;
import model.Event;
import model.Participant;
import model.Registration;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Business logic for event registrations.
 *
 * Key responsibilities:
 *   1. Check hall capacity before allowing a new registration.
 *   2. Prevent duplicate registrations (same participant + same event).
 *   3. Generate a unique QR code token for each registration.
 *   4. Validate QR tokens during entrance scanning.
 */
public class RegistrationService {

    private final RegistrationDAO registrationDAO;
    private final EventDAO        eventDAO;
    private final ParticipantDAO  participantDAO;

    public RegistrationService() {
        this.registrationDAO = new RegistrationDAO();
        this.eventDAO        = new EventDAO();
        this.participantDAO  = new ParticipantDAO();
    }

    /**
     * Registers a participant to an event.
     *
     * Steps:
     *   1. Verify the event exists.
     *   2. Verify the participant exists.
     *   3. Check if participant is already registered.
     *   4. Check if hall has available capacity.
     *   5. Generate a unique QR token.
     *   6. Save the registration.
     *
     * @param eventId       the event to register to
     * @param participantId the participant registering
     * @return the new Registration object (including generated QR token)
     * @throws SQLException             if a database error occurs
     * @throws IllegalArgumentException if validation fails or capacity is full
     */
    public Registration register(int eventId, int participantId) throws SQLException {

        // Step 1: Verify event exists and get capacity info
        Event event = eventDAO.findById(eventId);
        if (event == null)
            throw new IllegalArgumentException("Event not found: id=" + eventId);

        // Step 2: Verify participant exists
        Participant participant = participantDAO.findById(participantId);
        if (participant == null)
            throw new IllegalArgumentException("Participant not found: id=" + participantId);

        // Step 3: Check for duplicate registration
        if (registrationDAO.exists(eventId, participantId))
            throw new IllegalArgumentException("You are already registered for this event.");

        // Step 4: Check hall capacity
        int currentCount = registrationDAO.countByEventId(eventId);
        if (currentCount >= event.getHallCapacity())
            throw new IllegalArgumentException("Sorry, this event is fully booked. Hall capacity: " + event.getHallCapacity());

        // Step 5: Generate a unique QR code token (UUID-based)
        String qrToken = "QR-" + UUID.randomUUID().toString().toUpperCase();

        // Step 6: Save the registration
        Registration reg = new Registration(0, eventId, participantId, null, qrToken);
        int newId = registrationDAO.insert(reg);
        reg.setId(newId);
        reg.setEventTitle(event.getTitle());
        reg.setEventDate(event.getEventDate());
        reg.setParticipantName(participant.getFullName());
        reg.setRegisteredAt(java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        return reg;
    }

    /**
     * Returns all registrations for a specific event.
     * Used by admins to see who is attending.
     *
     * @param eventId the event's ID
     * @return list of registrations
     * @throws SQLException if a database error occurs
     */
    public List<Registration> getRegistrationsForEvent(int eventId) throws SQLException {
        return registrationDAO.findByEventId(eventId);
    }

    /**
     * Returns all registrations for a specific participant.
     * Used by users to view their own registered events.
     *
     * @param participantId the participant's ID
     * @return list of registrations
     * @throws SQLException if a database error occurs
     */
    public List<Registration> getRegistrationsForParticipant(int participantId) throws SQLException {
        return registrationDAO.findByParticipantId(participantId);
    }

    /**
     * Cancels (deletes) a registration by its ID.
     *
     * @param registrationId the registration to cancel
     * @throws SQLException if a database error occurs
     */
    public void cancelRegistration(int registrationId) throws SQLException {
        registrationDAO.delete(registrationId);
    }

    /**
     * Validates a QR code token scanned at the event entrance.
     * Returns the matching registration details if valid.
     *
     * @param token the QR code token scanned from the participant's QR code
     * @return the Registration if found and valid
     * @throws SQLException             if a database error occurs
     * @throws IllegalArgumentException if the token is not found (invalid QR code)
     */
    public Registration validateQRToken(String token) throws SQLException {
        Registration reg = registrationDAO.findByToken(token);
        if (reg == null)
            throw new IllegalArgumentException("Invalid QR code. Registration not found.");
        return reg;
    }
}

package service;

import db.EventDAO;
import db.HallDAO;
import model.Event;
import model.Hall;
import util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;

/**
 * Business logic for event management.
 * Sits between the API/Web layer and the DAO layer.
 * Validates input before calling the DAO, and throws descriptive errors.
 */
public class EventService {

    private final EventDAO eventDAO;
    private final HallDAO  hallDAO;

    public EventService() {
        this.eventDAO = new EventDAO();
        this.hallDAO  = new HallDAO();
    }

    /**
     * Returns all events with hall info and registration count.
     *
     * @return list of all events
     * @throws SQLException if a database error occurs
     */
    public List<Event> getAllEvents() throws SQLException {
        return eventDAO.findAll();
    }

    /**
     * Returns a single event by ID.
     *
     * @param id the event's ID
     * @return the Event object
     * @throws SQLException if a database error occurs
     * @throws IllegalArgumentException if event not found
     */
    public Event getEventById(int id) throws SQLException {
        Event event = eventDAO.findById(id);
        if (event == null) {
            throw new IllegalArgumentException("Event not found: id=" + id);
        }
        return event;
    }

    /**
     * Creates a new event after validating all fields.
     *
     * @param title       event title (required, not empty)
     * @param description optional description
     * @param eventDate   date in YYYY-MM-DD format
     * @param eventTime   time in HH:MM format
     * @param hallId      ID of an existing hall
     * @param createdBy   user ID of the admin creating the event
     * @return the ID of the newly created event
     * @throws SQLException             if a database error occurs
     * @throws IllegalArgumentException if validation fails
     */
    public int createEvent(String title, String description, String eventDate,
                           String eventTime, int hallId, int createdBy)
            throws SQLException {

        // Validate inputs
        if (!ValidationUtil.isNotEmpty(title, "title"))
            throw new IllegalArgumentException("Event title is required.");
        if (!ValidationUtil.isValidDate(eventDate))
            throw new IllegalArgumentException("Invalid date format. Use YYYY-MM-DD.");
        if (!ValidationUtil.isValidTime(eventTime))
            throw new IllegalArgumentException("Invalid time format. Use HH:MM.");

        // Check that the hall exists
        Hall hall = hallDAO.findById(hallId);
        if (hall == null)
            throw new IllegalArgumentException("Hall not found: id=" + hallId);

        Event event = new Event(0, title, description, eventDate, eventTime, hallId, createdBy);
        return eventDAO.insert(event);
    }

    /**
     * Updates an existing event's details.
     * Only admins should call this method (enforced at the Servlet level).
     *
     * @param id          the event ID to update
     * @param title       new title
     * @param description new description
     * @param eventDate   new date in YYYY-MM-DD
     * @param eventTime   new time in HH:MM
     * @param hallId      new hall ID
     * @throws SQLException             if a database error occurs
     * @throws IllegalArgumentException if validation fails or event/hall not found
     */
    public void updateEvent(int id, String title, String description,
                            String eventDate, String eventTime, int hallId)
            throws SQLException {

        // Check event exists
        Event existing = eventDAO.findById(id);
        if (existing == null)
            throw new IllegalArgumentException("Event not found: id=" + id);

        // Validate inputs
        if (!ValidationUtil.isNotEmpty(title, "title"))
            throw new IllegalArgumentException("Event title is required.");
        if (!ValidationUtil.isValidDate(eventDate))
            throw new IllegalArgumentException("Invalid date format. Use YYYY-MM-DD.");
        if (!ValidationUtil.isValidTime(eventTime))
            throw new IllegalArgumentException("Invalid time format. Use HH:MM.");

        // Check hall exists
        Hall hall = hallDAO.findById(hallId);
        if (hall == null)
            throw new IllegalArgumentException("Hall not found: id=" + hallId);

        existing.setTitle(title);
        existing.setDescription(description);
        existing.setEventDate(eventDate);
        existing.setEventTime(eventTime);
        existing.setHallId(hallId);
        eventDAO.update(existing);
    }

    /**
     * Deletes an event by ID.
     * All registrations for this event are deleted automatically (DB CASCADE).
     *
     * @param id the event ID to delete
     * @throws SQLException             if a database error occurs
     * @throws IllegalArgumentException if event not found
     */
    public void deleteEvent(int id) throws SQLException {
        Event existing = eventDAO.findById(id);
        if (existing == null)
            throw new IllegalArgumentException("Event not found: id=" + id);

        eventDAO.delete(id);
    }
}

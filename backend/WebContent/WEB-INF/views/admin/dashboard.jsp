<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Event" %>
<%@ page import="model.Hall" %>
<%@ page import="model.User" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Admin Dashboard — Event Management</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<nav class="navbar">
    <span class="nav-title">Admin Dashboard</span>
    <div class="nav-links">
        <span>Hello, <strong>${user.username}</strong> (Admin)</span>
        <a href="${pageContext.request.contextPath}/logout" class="btn btn-sm">Logout</a>
    </div>
</nav>

<div class="container">

    <%-- EVENTS SECTION --%>
    <div class="section-header">
        <h2>Events</h2>
        <button class="btn btn-primary" onclick="showModal('addEventModal')">+ Add Event</button>
    </div>

    <%
        List<Event> events = (List<Event>) request.getAttribute("events");
        List<Hall>  halls  = (List<Hall>)  request.getAttribute("halls");
        if (events == null || events.isEmpty()) {
    %>
        <p class="no-data">No events yet.</p>
    <%
        } else {
            for (Event event : events) {
    %>
        <div class="event-card">
            <div class="event-header">
                <h3><%= event.getTitle() %></h3>
                <div class="card-actions">
                    <button class="btn btn-sm" onclick="editEvent(<%= event.getId() %>,
                        '<%= event.getTitle().replace("'", "\\'") %>',
                        '<%= event.getEventDate() %>',
                        '<%= event.getEventTime().substring(0, 5) %>',
                        <%= event.getHallId() %>)">Edit</button>
                    <button class="btn btn-danger btn-sm" onclick="deleteEvent(<%= event.getId() %>)">Delete</button>
                </div>
            </div>
            <div class="event-meta">
                <span>📅 <%= event.getEventDate() %> at <%= event.getEventTime().substring(0, 5) %></span>
                <span>📍 <%= event.getHallName() %> (capacity: <%= event.getHallCapacity() %>)</span>
                <span>👥 <%= event.getRegistrationCount() %> registered</span>
            </div>
        </div>
    <%
            }
        }
    %>

    <hr>

    <%-- HALLS SECTION --%>
    <div class="section-header">
        <h2>Halls</h2>
        <button class="btn btn-primary" onclick="showModal('addHallModal')">+ Add Hall</button>
    </div>

    <table class="data-table">
        <thead>
            <tr><th>ID</th><th>Name</th><th>Capacity</th><th>Location</th><th>Actions</th></tr>
        </thead>
        <tbody>
        <%
            if (halls != null) {
                for (Hall hall : halls) {
        %>
            <tr>
                <td><%= hall.getId() %></td>
                <td><%= hall.getName() %></td>
                <td><%= hall.getCapacity() %></td>
                <td><%= hall.getLocation() %></td>
                <td>
                    <button class="btn btn-danger btn-sm"
                            onclick="deleteHall(<%= hall.getId() %>)">Delete</button>
                </td>
            </tr>
        <%
                }
            }
        %>
        </tbody>
    </table>
</div>

<%-- ADD EVENT MODAL --%>
<div id="addEventModal" class="modal" style="display:none;">
    <div class="modal-content">
        <h3>Add New Event</h3>
        <form id="addEventForm">
            <div class="form-group">
                <label>Title</label>
                <input type="text" name="title" required>
            </div>
            <div class="form-group">
                <label>Description</label>
                <textarea name="description" rows="3"></textarea>
            </div>
            <div class="form-group">
                <label>Date</label>
                <input type="date" name="eventDate" required>
            </div>
            <div class="form-group">
                <label>Time</label>
                <input type="time" name="eventTime" required>
            </div>
            <div class="form-group">
                <label>Hall</label>
                <select name="hallId" required>
                    <% if (halls != null) { for (Hall h : halls) { %>
                        <option value="<%= h.getId() %>"><%= h.getName() %> (cap: <%= h.getCapacity() %>)</option>
                    <% } } %>
                </select>
            </div>
            <div class="modal-actions">
                <button type="button" class="btn" onclick="hideModal('addEventModal')">Cancel</button>
                <button type="submit" class="btn btn-primary">Save</button>
            </div>
        </form>
    </div>
</div>

<script>
const API = "${pageContext.request.contextPath}/api";

function showModal(id) { document.getElementById(id).style.display = 'flex'; }
function hideModal(id) { document.getElementById(id).style.display = 'none'; }

// Add event
document.getElementById('addEventForm').addEventListener('submit', function(e) {
    e.preventDefault();
    const data = new URLSearchParams(new FormData(this));
    fetch(API + '/events', { method: 'POST', body: data })
        .then(r => r.json())
        .then(json => {
            if (json.success) { location.reload(); }
            else { alert('Error: ' + json.error); }
        });
});

// Delete event
function deleteEvent(id) {
    if (!confirm('Delete this event? All registrations will also be deleted.')) return;
    fetch(API + '/events?id=' + id, { method: 'DELETE' })
        .then(r => r.json())
        .then(json => {
            if (json.success) location.reload();
            else alert('Error: ' + json.error);
        });
}

// Delete hall
function deleteHall(id) {
    if (!confirm('Delete this hall?')) return;
    fetch(API + '/halls?id=' + id, { method: 'DELETE' })
        .then(r => r.json())
        .then(json => {
            if (json.success) location.reload();
            else alert('Error: ' + json.error);
        });
}
</script>

</body>
</html>

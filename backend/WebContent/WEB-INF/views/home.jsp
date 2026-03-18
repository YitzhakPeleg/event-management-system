<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Event" %>
<%@ page import="model.User" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Events — Event Management</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<%-- Navigation bar --%>
<nav class="navbar">
    <span class="nav-title">Event Management System</span>
    <div class="nav-links">
        <span>Hello, <strong>${user.username}</strong></span>
        <a href="${pageContext.request.contextPath}/logout" class="btn btn-sm">Logout</a>
    </div>
</nav>

<div class="container">
    <h2>Upcoming Events</h2>

    <%-- Error message (from request attribute or flash session attribute) --%>
    <% String flashError = (String) session.getAttribute("flashError");
       if (flashError != null) { session.removeAttribute("flashError"); %>
        <div class="error-msg"><%= flashError %></div>
    <% } else if (request.getAttribute("error") != null) { %>
        <div class="error-msg">${error}</div>
    <% } %>

    <%-- Success message (after registration) --%>
    <% if (request.getParameter("registered") != null) { %>
        <div class="success-msg">You have successfully registered for the event!</div>
    <% } %>

    <%
        List<Event> events = (List<Event>) request.getAttribute("events");
        User currentUser   = (User) request.getAttribute("user");
        if (events == null || events.isEmpty()) {
    %>
        <p class="no-data">No upcoming events at the moment.</p>
    <%
        } else {
            for (Event event : events) {
    %>
        <div class="event-card">
            <div class="event-header">
                <h3><%= event.getTitle() %></h3>
                <span class="badge <%= event.hasAvailableSpots() ? "badge-green" : "badge-red" %>">
                    <%= event.hasAvailableSpots() ? "Open" : "Full" %>
                </span>
            </div>
            <p class="event-desc"><%= event.getDescription() != null ? event.getDescription() : "" %></p>
            <div class="event-meta">
                <span>📅 <%= event.getEventDate() %> at <%= event.getEventTime().substring(0, 5) %></span>
                <span>📍 <%= event.getHallName() %></span>
                <span>👥 <%= event.getRegistrationCount() %> / <%= event.getHallCapacity() %> registered</span>
            </div>
            <% if (event.hasAvailableSpots()) { %>
                <form action="${pageContext.request.contextPath}/register" method="post"
                      onsubmit="return confirmReg(this)">
                    <input type="hidden" name="eventId" value="<%= event.getId() %>">
                    <button type="submit" class="btn btn-primary">Register</button>
                </form>
            <% } else { %>
                <button class="btn btn-disabled" disabled>Fully Booked</button>
            <% } %>
        </div>
    <%
            }
        }
    %>
</div>

<script>
function confirmReg(form) {
    return confirm("Confirm registration for this event?");
}
</script>

</body>
</html>

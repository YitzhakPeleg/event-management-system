<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login — Event Management</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="login-page">

<div class="login-box">
    <h1>Event Management System</h1>
    <h2>Sign In</h2>

    <%-- Show error message if login failed --%>
    <% if (request.getAttribute("error") != null) { %>
        <div class="error-msg">${error}</div>
    <% } %>

    <form action="${pageContext.request.contextPath}/login" method="post">
        <div class="form-group">
            <label for="username">Username</label>
            <input type="text" id="username" name="username"
                   placeholder="Enter username" required autofocus>
        </div>
        <div class="form-group">
            <label for="password">Password</label>
            <input type="password" id="password" name="password"
                   placeholder="Enter password" required>
        </div>
        <button type="submit" class="btn btn-primary btn-full">Sign In</button>
    </form>
</div>

</body>
</html>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Registration" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Registration Confirmed</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .confirm-box {
            max-width: 520px;
            margin: 60px auto;
            background: white;
            border-radius: 10px;
            padding: 40px;
            box-shadow: 0 4px 16px rgba(0,0,0,0.10);
            text-align: center;
        }
        .check { font-size: 64px; margin-bottom: 12px; }
        .confirm-box h2 { color: #27ae60; margin-bottom: 8px; }
        .confirm-box p  { color: #555; margin: 6px 0; font-size: 16px; }
        .qr-box {
            background: #f4f6f9;
            border-radius: 8px;
            padding: 16px 24px;
            margin: 24px 0;
            font-family: monospace;
            font-size: 13px;
            word-break: break-all;
            color: #2c3e50;
            border: 2px dashed #bdc3c7;
        }
        .qr-label { font-size: 12px; color: #999; margin-bottom: 6px; }
    </style>
</head>
<body style="background:#f4f6f9;">

<%
    Registration reg = (Registration) request.getAttribute("registration");
%>

<div class="confirm-box">
    <div class="check">✅</div>
    <h2>Registration Confirmed!</h2>
    <p>You are registered for:</p>
    <p><strong><%= reg.getEventTitle() %></strong></p>
    <p>📅 <%= reg.getEventDate() %></p>
    <p>👤 <%= reg.getParticipantName() %></p>
    <p style="color:#999; font-size:13px;">Registered at: <%= reg.getRegisteredAt() %></p>

    <div class="qr-label">Your entrance QR token — show this at the door</div>
    <div class="qr-box"><%= reg.getQrCodeToken() %></div>

    <p style="font-size:13px; color:#999;">
        Your QR code will also appear in the Android app.
    </p>

    <a href="${pageContext.request.contextPath}/home" class="btn btn-primary" style="display:inline-block; margin-top:16px;">
        Back to Events
    </a>
</div>

</body>
</html>

<%--
    Document   : index
    Created on : 10 вер. 2026 р., 09:39:02
    Author     : samoylenko_d
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.nio.charset.Charset"%>
<%
    String viewName = (String) request.getAttribute("servlet");
    if (viewName == null) {
        viewName = "home.jsp";
    } else {
        viewName = viewName.toLowerCase() + ".jsp";
    }
    Charset charset = (Charset) request.getAttribute("charset");
    String charsetName = charset == null ? "undefined" : charset.name();
%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
        <link rel="stylesheet" href="css/style.css">
    </head>
    <body>
        <header>Java Webapp</header>

        <nav>
            <a href="home">Home</a>
            <a href="about">About</a>
            <a href="privacy">Privacy</a>
        </nav>

        <main>
            <jsp:include page="<%=viewName%>" />
        </main>

        <footer>
            &copy; 2026 java-webapp
            &middot;
            <a href="privacy">Privacy</a>
            &middot;
            Show in '<%= charsetName %>'
        </footer>
    </body>
</html>

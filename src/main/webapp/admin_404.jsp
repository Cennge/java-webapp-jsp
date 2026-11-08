<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="step.learning.java231web.util.Html"%>
<%
    String invalidRoute = (String) request.getAttribute("invalidRoute");
    String contextPath = request.getContextPath();
%>
<div class="admin-404">
    <h3>404 — внутрішній маршрут не знайдено</h3>
    <p>Розділу адміністративної панелі <code><%= Html.esc(invalidRoute) %></code> не існує.</p>
    <p>
        Скористайтеся меню вище або поверніться на
        <a href="<%= contextPath %>/admin/">головну сторінку адмінки</a>.
    </p>
</div>

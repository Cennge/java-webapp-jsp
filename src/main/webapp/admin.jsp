<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="step.learning.java231web.util.Html"%>
<%
    // Каркас адмінки: внутрішнє меню + представлення внутрішнього маршруту (admin_{page}.jsp)
    String adminPage = (String) request.getAttribute("adminPage");
    String adminView = (String) request.getAttribute("adminView");
    if (adminPage == null || adminView == null) {
        adminPage = "home";
        adminView = "/admin_home.jsp";
    }
    String contextPath = request.getContextPath();
    String formMessage = (String) request.getAttribute("formMessage");
    String formError = (String) request.getAttribute("formError");
    String dbError = (String) request.getAttribute("dbError");
%>
<h1>Адміністрування</h1>
<div class="admin-menu">
    <a href="<%= contextPath %>/admin/" class="<%= "home".equals(adminPage) ? "active" : "" %>">Home</a>
    <a href="<%= contextPath %>/admin/section" class="<%= "section".equals(adminPage) ? "active" : "" %>">Section</a>
    <a href="<%= contextPath %>/admin/product" class="<%= "product".equals(adminPage) ? "active" : "" %>">Product</a>
</div>

<% if (formMessage != null) { %>
    <div class="admin-alert ok">&#10004; <%= Html.esc(formMessage) %></div>
<% } %>
<% if (formError != null) { %>
    <div class="admin-alert fail">&#9888; <%= Html.esc(formError) %></div>
<% } %>
<% if (dbError != null) { %>
    <div class="admin-alert fail">&#9888; <%= Html.esc(dbError) %></div>
<% } %>

<jsp:include page="<%= adminView %>" />

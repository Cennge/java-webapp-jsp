<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="step.learning.java231web.data.dto.Product"%>
<%@page import="step.learning.java231web.data.dto.ProductSection"%>
<%@page import="step.learning.java231web.rest.RestPagination"%>
<%@page import="step.learning.java231web.util.Html"%>
<%!
    String ctx;

    /** Зображення розділу або плитка-заглушка з першою літерою (колір - від слагу) */
    String thumb(ProductSection s, String cssClass) {
        if (s.getImageUrl() != null) {
            return "<img class=\"" + cssClass + "\" src=\"" + ctx + "/storage/" + Html.esc(s.getImageUrl())
                    + "\" alt=\"\" loading=\"lazy\">";
        }
        int hue = Math.floorMod(s.getSlug().hashCode(), 360);
        String letter = s.getTitle().isEmpty() ? "?" : s.getTitle().substring(0, 1).toUpperCase();
        return "<span class=\"" + cssClass + " thumb-placeholder\" style=\"--hue:" + hue + "\">"
                + Html.esc(letter) + "</span>";
    }

    String href(ProductSection s) {
        return ctx + "/catalog/" + Html.esc(s.getSlug());
    }

    /** "3 підгрупи", "5 підгруп", "1 підгрупа" */
    String subgroups(int n) {
        int m10 = n % 10, m100 = n % 100;
        String w = m10 == 1 && m100 != 11 ? "підгрупа"
                : m10 >= 2 && m10 <= 4 && (m100 < 12 || m100 > 14) ? "підгрупи" : "підгруп";
        return n + " " + w;
    }
%>
<%
    ctx = request.getContextPath();
    List<ProductSection> groups = (List<ProductSection>) request.getAttribute("groups");
    ProductSection section = (ProductSection) request.getAttribute("section");
    List<ProductSection> breadcrumbs = (List<ProductSection>) request.getAttribute("breadcrumbs");
    RestPagination pagination = (RestPagination) request.getAttribute("pagination");
    String notFound = (String) request.getAttribute("notFound");
    String dbError = (String) request.getAttribute("dbError");
%>
<% if (dbError != null) { %>
    <div class="admin-alert fail">&#9888; <%= Html.esc(dbError) %></div>
<% } else if (notFound != null) { %>
    <h1>Каталог</h1>
    <div class="admin-404">
        <h3>Товарну групу <code><%= Html.esc(notFound) %></code> не знайдено</h3>
        <p>Поверніться до <a href="<%= ctx %>/catalog">переліку груп</a>.</p>
    </div>
<% } else if (groups != null) { %>
    <%-- ===== Основні товарні групи ===== --%>
    <h1>Каталог</h1>
    <div class="group-grid">
    <% for (ProductSection g : groups) { %>
        <article class="group-card">
            <a class="group-card-main" href="<%= href(g) %>">
                <%= thumb(g, "group-card-img") %>
                <span class="group-card-title"><%= Html.esc(g.getTitle()) %></span>
                <% if (!g.getChildren().isEmpty()) { %>
                    <span class="group-card-meta"><%= subgroups(g.getChildren().size()) %></span>
                <% } %>
            </a>
            <% if (!g.getChildren().isEmpty()) { %>
                <ul class="subgroup-chips" aria-label="Підгрупи">
                <% for (ProductSection c : g.getChildren()) { %>
                    <li><a class="subgroup-chip" href="<%= href(c) %>"><%= thumb(c, "subgroup-chip-img") %><%= Html.esc(c.getTitle()) %></a></li>
                <% } %>
                </ul>
            <% } %>
        </article>
    <% } %>
    </div>
<% } else if (section != null) { %>
    <%-- ===== Товарна група ===== --%>
    <div class="breadcrumbs" role="navigation" aria-label="Шлях">
        <a href="<%= ctx %>/catalog">Каталог</a>
        <% for (ProductSection b : breadcrumbs) { %>
            <span>&rsaquo;</span><a href="<%= href(b) %>"><%= Html.esc(b.getTitle()) %></a>
        <% } %>
        <span>&rsaquo;</span><span aria-current="page"><%= Html.esc(section.getTitle()) %></span>
    </div>
    <div class="section-head">
        <%= thumb(section, "section-head-img") %>
        <div>
            <h1><%= Html.esc(section.getTitle()) %></h1>
            <% if (section.getDescription() != null) { %><p><%= Html.esc(section.getDescription()) %></p><% } %>
        </div>
    </div>

    <% if (!section.getChildren().isEmpty()) { %>
    <section class="subgroups" aria-labelledby="subgroups-title">
        <h2 id="subgroups-title">Підгрупи</h2>
        <div class="subgroup-grid">
        <% for (ProductSection c : section.getChildren()) { %>
            <a class="subgroup-card" href="<%= href(c) %>">
                <%= thumb(c, "subgroup-card-img") %>
                <span>
                    <span class="subgroup-card-title"><%= Html.esc(c.getTitle()) %></span>
                    <% if (!c.getChildren().isEmpty()) { %>
                        <span class="subgroup-card-meta"><%= subgroups(c.getChildren().size()) %></span>
                    <% } %>
                </span>
                <span class="subgroup-card-arrow" aria-hidden="true">&rsaquo;</span>
            </a>
        <% } %>
        </div>
    </section>
    <% } %>

    <h2>Товари</h2>
    <% if (section.getProducts().isEmpty()) { %>
        <p class="muted">У цій групі поки немає товарів<%= section.getChildren().isEmpty() ? "" : " — перегляньте підгрупи вище" %>.</p>
    <% } else { %>
        <div class="product-grid">
        <% for (Product p : section.getProducts()) { %>
            <a class="product-card" href="<%= ctx %>/product/<%= Html.esc(p.getSlug()) %>">
                <% if (p.getImageUrl() != null) { %>
                    <img src="<%= ctx %>/storage/<%= Html.esc(p.getImageUrl()) %>" alt="" loading="lazy">
                <% } else { %>
                    <span class="product-card-noimg">без фото</span>
                <% } %>
                <span class="product-card-title"><%= Html.esc(p.getTitle()) %></span>
                <span class="product-card-price"><%= String.format(java.util.Locale.ROOT, "%,.2f", p.getPrice()).replace(',', ' ') %> ₴</span>
            </a>
        <% } %>
        </div>
        <% if (pagination != null && pagination.getTotalPages() > 1) { %>
            <div class="pager" role="navigation">
            <% for (int i = 1; i <= pagination.getTotalPages(); i++) { %>
                <% if (i == pagination.getPage()) { %><span aria-current="page"><%= i %></span>
                <% } else { %><a href="?page=<%= i %>"><%= i %></a><% } %>
            <% } %>
            </div>
        <% } %>
    <% } %>
<% } %>

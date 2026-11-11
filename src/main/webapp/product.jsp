<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.math.BigDecimal"%>
<%@page import="java.util.List"%>
<%@page import="step.learning.java231web.data.dto.Product"%>
<%@page import="step.learning.java231web.data.dto.ProductVersion"%>
<%@page import="step.learning.java231web.util.Html"%>
<%!
    String money(BigDecimal v) {
        return String.format(java.util.Locale.ROOT, "%,.2f", v).replace(',', ' ') + " ₴";
    }
    String stock(int n) {
        return n > 0 ? "<span class=\"stock-ok\">в наявності: " + n + "</span>"
                     : "<span class=\"stock-none\">немає в наявності</span>";
    }
    String img(String url) {
        return url != null
                ? "<img src=\"" + Html.esc(url) + "\" alt=\"\">"
                : "<span class=\"product-card-noimg\">без фото</span>";
    }
%>
<%
    String ctx = request.getContextPath();
    Product product = (Product) request.getAttribute("product");
    String notFound = (String) request.getAttribute("notFound");
    String dbError = (String) request.getAttribute("dbError");
%>
<% if (dbError != null) { %>
    <div class="admin-alert fail">&#9888; <%= Html.esc(dbError) %></div>
<% } else if (notFound != null) { %>
    <h1>Товар не знайдено</h1>
    <p>Товару <code><%= Html.esc(notFound) %></code> немає. Поверніться до <a href="<%= ctx %>/catalog">каталогу</a>.</p>
<% } else {
       List<ProductVersion> versions = product.getVersions();
       boolean multi = versions.size() > 1; %>
    <div class="product-crumbs"><a href="<%= ctx %>/catalog">Каталог</a> <span>&rsaquo;</span> <%= Html.esc(product.getTitle()) %></div>

    <% if (multi) { %>
        <%-- декілька версій: спільна назва - один раз заголовком, версії - лише додатками --%>
        <h1 class="product-title"><%= Html.esc(product.getTitle()) %></h1>
        <% if (product.getDescription() != null) { %><p class="product-desc"><%= Html.esc(product.getDescription()) %></p><% } %>
        <p class="muted">Версій: <%= versions.size() %></p>
        <div class="version-grid">
        <% for (ProductVersion v : versions) { %>
            <div class="version-card">
                <%= img(v.getImageUrl()) %>
                <span class="version-addon"><%= v.getTitleAddon() == null || v.getTitleAddon().isBlank() ? "Базова версія" : Html.esc(v.getTitleAddon()) %></span>
                <% if (v.getDescriptionAddon() != null) { %><span class="muted"><%= Html.esc(v.getDescriptionAddon()) %></span><% } %>
                <span class="product-card-price"><%= money(v.getPrice()) %></span>
                <%= stock(v.getStock()) %>
            </div>
        <% } %>
        </div>
    <% } else {
           // одна версія (або жодної) - структура без змін: повна назва, ціна, наявність
           ProductVersion v = versions.isEmpty() ? null : versions.get(0);
           String title = v == null || v.getTitleAddon() == null ? product.getTitle() : product.getTitle() + " " + v.getTitleAddon();
           String desc = v == null || v.getDescriptionAddon() == null ? product.getDescription()
                   : (product.getDescription() == null ? "" : product.getDescription() + " ") + v.getDescriptionAddon(); %>
        <div class="product-single">
            <%= img(v == null ? product.getImageUrl() : v.getImageUrl()) %>
            <div>
                <h1 class="product-title"><%= Html.esc(title) %></h1>
                <% if (desc != null) { %><p class="product-desc"><%= Html.esc(desc) %></p><% } %>
                <p class="product-card-price"><%= money(v == null ? product.getPrice() : v.getPrice()) %></p>
                <p><%= stock(v == null ? product.getStock() : v.getStock()) %></p>
            </div>
        </div>
    <% } %>
<% } %>

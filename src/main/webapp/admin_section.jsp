<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="step.learning.java231web.data.dto.ProductSection"%>
<%@page import="step.learning.java231web.util.FormView"%>
<%@page import="step.learning.java231web.util.Html"%>
<%!
    // дерево розділів -> <option> з відступом за глибиною
    void options(StringBuilder sb, List<ProductSection> nodes, int depth, FormView fv) {
        for (ProductSection s : nodes) {
            sb.append("<option value=\"").append(Html.esc(s.getId())).append('"')
              .append(fv.selected("section-parent-id", s.getId()) ? " selected" : "").append('>')
              .append("&nbsp;&nbsp;&nbsp;".repeat(depth)).append(Html.esc(s.getTitle()))
              .append("</option>");
            options(sb, s.getChildren(), depth + 1, fv);
        }
    }
    // дерево розділів -> рядки таблиці
    void rows(StringBuilder sb, List<ProductSection> nodes, int depth) {
        for (ProductSection s : nodes) {
            sb.append("<tr><td>").append("&nbsp;&nbsp;&nbsp;&nbsp;".repeat(depth))
              .append(depth > 0 ? "&#8627; " : "").append(Html.esc(s.getTitle())).append("</td>")
              .append("<td><code>").append(Html.esc(s.getSlug())).append("</code></td>")
              .append("<td class=\"num\">").append(s.getOrderInPrice()).append("</td>")
              .append("<td>").append(s.getImageUrl() != null ? "&#10004;" : "").append("</td></tr>");
            rows(sb, s.getChildren(), depth + 1);
        }
    }
%>
<%
    List<ProductSection> tree = (List<ProductSection>) request.getAttribute("sectionTree");
    FormView fv = FormView.of(request, "section");
    StringBuilder parentOptions = new StringBuilder();
    StringBuilder tableRows = new StringBuilder();
    if (tree != null) {
        options(parentOptions, tree, 0, fv);
        rows(tableRows, tree, 0);
    }
%>
<div class="admin-columns">
    <form method="post" action="<%= request.getContextPath() %>/admin/section"
          enctype="multipart/form-data" class="admin-form" novalidate>
        <h3>Новий розділ</h3>
        <div class="<%= fv.invalid("section-parent-id") %>">
            <label for="section-parent-id">Батьківський розділ <span class="hint">(необов'язково)</span></label>
            <select id="section-parent-id" name="section-parent-id">
                <option value="">— кореневий розділ —</option>
                <%= parentOptions %>
            </select>
            <%= fv.error("section-parent-id") %>
        </div>
        <div class="<%= fv.invalid("section-title") %>">
            <label for="section-title">Назва</label>
            <input type="text" id="section-title" name="section-title" value="<%= fv.value("section-title") %>"
                   placeholder="Побутова техніка" />
            <%= fv.error("section-title") %>
        </div>
        <div class="<%= fv.invalid("section-description") %>">
            <label for="section-description">Опис</label>
            <textarea id="section-description" name="section-description" rows="3"
                      placeholder="Техніка для дому"><%= fv.value("section-description") %></textarea>
            <%= fv.error("section-description") %>
        </div>
        <div class="row">
            <div class="<%= fv.invalid("section-slug") %>" style="grid-column: span 2">
                <label for="section-slug">Слаг</label>
                <input type="text" id="section-slug" name="section-slug" value="<%= fv.value("section-slug") %>"
                       placeholder="home-appliances" />
                <%= fv.error("section-slug") %>
            </div>
            <div class="<%= fv.invalid("section-order") %>">
                <label for="section-order">Порядок</label>
                <input type="number" id="section-order" name="section-order" value="<%= fv.value("section-order") %>"
                       placeholder="10" />
                <%= fv.error("section-order") %>
            </div>
        </div>
        <div class="<%= fv.invalid("section-image") %>">
            <label for="section-image">Зображення</label>
            <input type="file" id="section-image" name="section-image" accept="image/*" />
            <%= fv.error("section-image") %>
        </div>
        <button type="submit">Додати розділ</button>
    </form>

    <div>
        <h3>Розділи</h3>
        <table class="admin-table">
            <thead><tr><th>Назва</th><th>Слаг</th><th>Порядок</th><th>Фото</th></tr></thead>
            <tbody><%= tableRows %></tbody>
        </table>
    </div>
</div>

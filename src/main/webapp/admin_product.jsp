<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="step.learning.java231web.data.dto.Product"%>
<%@page import="step.learning.java231web.data.dto.ProductSection"%>
<%@page import="step.learning.java231web.util.FormView"%>
<%@page import="step.learning.java231web.util.Html"%>
<%!
    void sectionOptions(StringBuilder sb, List<ProductSection> nodes, int depth, FormView fv) {
        for (ProductSection s : nodes) {
            sb.append("<option value=\"").append(Html.esc(s.getId())).append('"')
              .append(fv.selected("product-section-id", s.getId()) ? " selected" : "").append('>')
              .append("&nbsp;&nbsp;&nbsp;".repeat(depth)).append(Html.esc(s.getTitle()))
              .append("</option>");
            sectionOptions(sb, s.getChildren(), depth + 1, fv);
        }
    }
%>
<%
    List<ProductSection> tree = (List<ProductSection>) request.getAttribute("sectionTree");
    List<Product> products = (List<Product>) request.getAttribute("products");
    FormView pf = FormView.of(request, "product");
    FormView vf = FormView.of(request, "version");
    String contextPath = request.getContextPath();

    StringBuilder options = new StringBuilder();
    if (tree != null) {
        sectionOptions(options, tree, 0, pf);
    }
%>
<div class="admin-columns">
    <form method="post" action="<%= contextPath %>/admin/product"
          enctype="multipart/form-data" class="admin-form" novalidate>
        <h3>Новий товар</h3>
        <div class="<%= pf.invalid("product-section-id") %>">
            <label for="product-section-id">Розділ</label>
            <select id="product-section-id" name="product-section-id">
                <option value="">— оберіть розділ —</option>
                <%= options %>
            </select>
            <%= pf.error("product-section-id") %>
        </div>
        <div class="<%= pf.invalid("product-title") %>">
            <label for="product-title">Назва</label>
            <input type="text" id="product-title" name="product-title" value="<%= pf.value("product-title") %>"
                   placeholder="iPhone 18 Pro Max" />
            <%= pf.error("product-title") %>
        </div>
        <div class="<%= pf.invalid("product-description") %>">
            <label for="product-description">Опис</label>
            <textarea id="product-description" name="product-description" rows="2"
                      placeholder="Опис товару"><%= pf.value("product-description") %></textarea>
            <%= pf.error("product-description") %>
        </div>
        <div class="<%= pf.invalid("product-slug") %>">
            <label for="product-slug">Слаг</label>
            <input type="text" id="product-slug" name="product-slug" value="<%= pf.value("product-slug") %>"
                   placeholder="iphone-18-pro-max" />
            <%= pf.error("product-slug") %>
        </div>
        <div class="row">
            <div class="<%= pf.invalid("product-price") %>">
                <label for="product-price">Ціна, ₴</label>
                <input type="text" inputmode="decimal" id="product-price" name="product-price"
                       value="<%= pf.value("product-price") %>" placeholder="15999.00" />
                <%= pf.error("product-price") %>
            </div>
            <div class="<%= pf.invalid("product-stock") %>">
                <label for="product-stock">На складі</label>
                <input type="number" id="product-stock" name="product-stock" value="<%= pf.value("product-stock") %>"
                       placeholder="10" />
                <%= pf.error("product-stock") %>
            </div>
            <div class="<%= pf.invalid("product-order") %>">
                <label for="product-order">Порядок</label>
                <input type="number" id="product-order" name="product-order" value="<%= pf.value("product-order") %>"
                       placeholder="1" />
                <%= pf.error("product-order") %>
            </div>
        </div>
        <div class="<%= pf.invalid("product-image") %>">
            <label for="product-image">Зображення</label>
            <input type="file" id="product-image" name="product-image" accept="image/*" />
            <%= pf.error("product-image") %>
        </div>
        <button type="submit">Додати товар</button>
    </form>

    <form method="post" action="<%= contextPath %>/admin/product-version"
          enctype="multipart/form-data" class="admin-form" novalidate>
        <h3>Нова версія товару</h3>
        <div class="<%= vf.invalid("product-id") %>">
            <label for="product-id">Товар</label>
            <select id="product-id" name="product-id">
                <option value="">— оберіть товар —</option>
                <% if (products != null) for (Product p : products) { %>
                    <option value="<%= Html.esc(p.getId()) %>" <%= vf.selected("product-id", p.getId()) ? "selected" : "" %>>
                        <%= Html.esc(p.getTitle()) %>
                    </option>
                <% } %>
            </select>
            <%= vf.error("product-id") %>
        </div>
        <div class="<%= vf.invalid("version-title") %>">
            <label for="version-title">Доповнення до назви</label>
            <input type="text" id="version-title" name="version-title" value="<%= vf.value("version-title") %>"
                   placeholder="Cosmic Red" />
            <%= vf.error("version-title") %>
        </div>
        <div class="<%= vf.invalid("version-description") %>">
            <label for="version-description">Доповнення до опису</label>
            <textarea id="version-description" name="version-description" rows="2"
                      placeholder="Колір: червоний"><%= vf.value("version-description") %></textarea>
            <%= vf.error("version-description") %>
        </div>
        <div class="<%= vf.invalid("version-slug") %>">
            <label for="version-slug">Доповнення до слагу</label>
            <input type="text" id="version-slug" name="version-slug" value="<%= vf.value("version-slug") %>"
                   placeholder="cosmic-red" />
            <%= vf.error("version-slug") %>
        </div>
        <div class="row">
            <div class="<%= vf.invalid("version-price") %>">
                <label for="version-price">Ціна, ₴</label>
                <input type="text" inputmode="decimal" id="version-price" name="version-price"
                       value="<%= vf.value("version-price") %>" placeholder="16999.00" />
                <%= vf.error("version-price") %>
            </div>
            <div class="<%= vf.invalid("version-stock") %>">
                <label for="version-stock">На складі</label>
                <input type="number" id="version-stock" name="version-stock" value="<%= vf.value("version-stock") %>"
                       placeholder="5" />
                <%= vf.error("version-stock") %>
            </div>
            <div class="<%= vf.invalid("version-order") %>">
                <label for="version-order">У прайсі</label>
                <input type="number" id="version-order" name="version-order" value="<%= vf.value("version-order") %>"
                       placeholder="2" />
                <%= vf.error("version-order") %>
            </div>
        </div>
        <div class="<%= vf.invalid("version-image") %>">
            <label for="version-image">Зображення <span class="hint">(необов'язково — інакше товарне)</span></label>
            <input type="file" id="version-image" name="version-image" accept="image/*" />
            <%= vf.error("version-image") %>
        </div>
        <button type="submit">Додати версію</button>
    </form>
</div>

<h2>Товари</h2>
<table class="admin-table">
    <thead><tr><th></th><th>Назва</th><th>Слаг</th><th>Ціна, ₴</th><th>На складі</th></tr></thead>
    <tbody>
    <% if (products != null) for (Product p : products) { %>
        <tr>
            <td><% if (p.getImageUrl() != null) { %>
                <img src="<%= contextPath %>/storage/<%= Html.esc(p.getImageUrl()) %>" alt="" />
            <% } %></td>
            <td><a href="<%= contextPath %>/product/<%= Html.esc(p.getSlug()) %>"><%= Html.esc(p.getTitle()) %></a></td>
            <td><code><%= Html.esc(p.getSlug()) %></code></td>
            <td class="num"><%= p.getPrice() %></td>
            <td class="num"><%= p.getStock() %></td>
        </tr>
    <% } %>
    </tbody>
</table>

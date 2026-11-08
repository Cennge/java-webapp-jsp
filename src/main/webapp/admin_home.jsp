<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="step.learning.java231web.data.dto.ProductSection"%>
<%@page import="step.learning.java231web.data.dto.Product"%>
<%
    List<ProductSection> sections = (List<ProductSection>) request.getAttribute("sections");
    List<ProductSection> tree = (List<ProductSection>) request.getAttribute("sectionTree");
    Integer productsCount = (Integer) request.getAttribute("productsCount");
    String contextPath = request.getContextPath();
%>
<p>Головна панель адміністрування каталогу: розділи, товари та їх версії.</p>

<div class="admin-stats">
    <div class="admin-stat">
        <b><%= sections != null ? sections.size() : "—" %></b>
        розділів (з них кореневих: <%= tree != null ? tree.size() : "—" %>)
    </div>
    <div class="admin-stat">
        <b><%= productsCount != null ? productsCount : "—" %></b>
        товарів
    </div>
</div>

<h2>REST API каталогу</h2>
<ul>
    <li><a href="<%= contextPath %>/section"><code>GET /section</code></a> — дерево розділів з пагінацією</li>
    <li><a href="<%= contextPath %>/section/android"><code>GET /section/{slug}</code></a> — розділ з підрозділами та товарами</li>
    <li><a href="<%= contextPath %>/product/iphone-18-pro-max"><code>GET /product/{slug}</code></a> — товар з версіями</li>
    <li><code>GET /storage/{file}</code> — завантажені зображення</li>
</ul>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="step.learning.java231web.models.AuthUser"%>
<%@page import="step.learning.java231web.util.Html"%>
<%
    String str = "The String";
    int x = 10;
%>
<!DOCTYPE html>
<html lang="uk">
    <head>
        <meta charset="UTF-8">
        <title>Java Webapp</title>
    </head>
    <body>
        <h1>Java Webapp</h1>
        <p>мдээээ.../p>
        <p>
            <%= str %>
            <%= x + 2 %>
        </p>
        <p>
            <% if (x % 2 == 0) { %>
            <b>Number <%= x %> is even</b>
            <% } else { %>
            <i>Number <%= x %> is odd</i>
            <% } %>
        </p>
        <ul>
            <% for (int i = 0; i < 10; ++i) { %>
            <% if (i == 0) { %>
            <li><%= i %> never odd or even</li>
            <% } else if (i % 2 == 0) { %>
            <li><%= i %> is even</li>
            <% } else { %>
            <li><%= i %> is odd</li>
            <% } %>
            <% } %>
        </ul>
        <%-- Статичне підключення файлу --%>
        <jsp:include page="fragment1.jsp" />

        <%-- Підключення файлу з передачею параметрів --%>
        <jsp:include page="WEB-INF/fragment2.jsp">
            <jsp:param name="str" value="<%=str%>" />
        </jsp:include>

        <h2>Сервлети - JSP контролери</h2>
        <p>
            Сервлети - спеціалізовані класи для серверних (мережевих)
            задач. Дочірні класи HTTP-призначення грають роль
            контролерів. Описуються в кодовій частині проекту
            (тоді як JSP - у webapp). Підключаються до сервера
            одним з трьох варіантів:
        </p>
        <ul>
            <li>
                За допомогою інструкцій у web.xml
            </li>
            <li>
                За допомогою анотацій @WebServlet
            </li>
            <li>
                Як частина інверсії управління
            </li>
        </ul>
        <h2>Авторизація (?ua=...)</h2>
        <% AuthUser user = (AuthUser) request.getAttribute(AuthUser.ATTRIBUTE);
           Object authError = request.getAttribute("authError"); %>
        <% if (user != null) { %>
            <p class="card timestamp-card">&#10004; Токен дійсний: <b><%= Html.esc(user.name()) %></b>
               (логін <code><%= Html.esc(user.login()) %></code>, роль <code><%= Html.esc(user.role()) %></code>,
               id <code><%= Html.esc(user.id()) %></code>)</p>
        <% } else if (authError != null) { %>
            <p class="card" style="border-left:4px solid #dc2626">&#9888; Токен недійсний: <%= Html.esc(((Exception) authError).getMessage()) %></p>
        <% } else { %>
            <p class="card">Токен не передано. Додайте до адреси <code>?ua={token}</code>
               (токен видає <code>GET /user</code> з Basic-авторизацією).</p>
        <% } %>

        <h2>Сервіси (IoC)</h2>
        <ul>
            <li>Hash (MD5 з "123"): <code><%= request.getAttribute("hash") %></code></li>
            <li>Timestamp, 10 цифр (секунди): <code><%= request.getAttribute("timestamp10") %></code></li>
            <li>Timestamp, 13 цифр (мілісекунди): <code><%= request.getAttribute("timestamp13") %></code></li>
        </ul>
        <p>
            Контроль проходження сервлету: <%= request.getAttribute("servlet") %>
        </p>
    </body>
</html>

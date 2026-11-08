<%--
    Document   : index
    Created on : 10 вер. 2026 р., 09:39:02
    Author     : ilyag
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.nio.charset.Charset"%>
<%@page import="step.learning.java231web.models.AuthUser"%>
<%@page import="step.learning.java231web.util.Html"%>
<%
    String viewName = (String) request.getAttribute("servlet");
    // шлях від кореня застосунку: сторінки з вкладеними адресами (/admin/product)
    // інакше шукали б представлення відносно /admin/
    if (viewName == null) {
        viewName = "/home.jsp";
    } else {
        viewName = "/" + viewName.toLowerCase() + ".jsp";
    }
    Charset charset = (Charset) request.getAttribute("charset");
    String charsetName = charset == null ? "undefined" : charset.name();

    String contextPath = request.getContextPath();  // частина адреси,
    // що відповідає за контекст - назва проєкту. Починається з "/",
    // завершується без "/"
%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
        <link rel="stylesheet" href="<%= contextPath %>/css/style.css">
    </head>
    <body>
        <header>
            Java Webapp
            <span id="authBox" class="auth-box"></span>
        </header>

        <nav>
            <a href="<%= contextPath %>/">Home</a>
            <a href="<%= contextPath %>/about">About</a>
            <a href="<%= contextPath %>/privacy">Privacy</a>
            <a href="<%= contextPath %>/gson">Gson</a>
            <a href="<%= contextPath %>/db">DB</a>
            <a href="<%= contextPath %>/back">Back API</a>
            <a href="<%= contextPath %>/catalog">Каталог</a>
            <a href="<%= contextPath %>/admin/">Admin</a>
            <a href="<%= contextPath %>/signup" id="navSignup">Реєстрація</a>
        </nav>

        <script>
        // Стан автентифікації фронтенду: { token, user } у localStorage.
        // Токен перевіряється сервером (GET /user/me) - після перезапуску сервера
        // чи закінчення терміну дії він стає недійсним і стан очищується
        window.appAuth = {
            key: "java-webapp-auth",
            get() {
                try { return JSON.parse(localStorage.getItem(this.key)); } catch (e) { return null; }
            },
            set(auth) {
                try { localStorage.setItem(this.key, JSON.stringify(auth)); } catch (e) {}
                this.render();
            },
            clear() {
                try { localStorage.removeItem(this.key); } catch (e) {}
                this.render();
            },
            render() {
                const box = document.getElementById("authBox");
                const auth = this.get();
                box.replaceChildren();
                document.getElementById("navSignup").style.display = auth ? "none" : "";
                if (!auth) return;
                const who = document.createElement("span");
                who.textContent = "Ви увійшли: " + auth.user.name + " (" + auth.user.login + ")";
                const out = document.createElement("button");
                out.textContent = "Вийти";
                out.onclick = () => this.clear();
                box.append(who, out);
            },
            verify() {
                const auth = this.get();
                if (!auth) return;
                fetch("<%= contextPath %>/user/me", { headers: { "Authorization": "Bearer " + auth.token } })
                    .then(r => r.json())
                    .then(j => { if (!j.status.isOk) this.clear(); })
                    .catch(() => {});
            }
        };
        appAuth.render();
        appAuth.verify();
        </script>

        <main>
            <jsp:include page="<%=viewName%>" />
        </main>

        <footer>
            &copy; 2026 java-webapp
            &middot;
            <a href="<%= contextPath %>/privacy">Privacy</a>
            &middot;
            Show in '<%= charsetName %>'
            &middot;
            Auth: <% AuthUser authUser = (AuthUser) request.getAttribute(AuthUser.ATTRIBUTE); %>
            <%= authUser == null ? "guest" : Html.esc(authUser.login() + " (" + authUser.role() + ")") %>
        </footer>
    </body>
</html>

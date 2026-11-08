<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String contextPath = request.getContextPath();
%>
<style>
    .signup-form { display: grid; gap: 14px; max-width: 460px; }
    .signup-form label { display: block; font-weight: bold; margin-bottom: 4px; }
    .signup-form input[type=text],
    .signup-form input[type=email],
    .signup-form input[type=password] {
        width: 100%; box-sizing: border-box; padding: 8px 12px;
        border: 1px solid #cbd5e1; border-radius: 4px; font-size: 1em;
    }
    .signup-form .invalid input { border-color: #dc2626; background: #fef2f2; }
    .signup-form .valid input { border-color: #16a34a; }
    .field-error { color: #dc2626; font-size: .88em; margin-top: 4px; min-height: 1em; }
    .field-error.ok { color: #16a34a; }
    .signup-form button {
        padding: 10px 18px; border: none; border-radius: 4px; cursor: pointer;
        background: #1e3a8a; color: #fff; font-weight: bold; font-size: 1em;
    }
    .signup-result { margin-top: 16px; padding: 10px 14px; border-radius: 6px; display: none; }
    .signup-result.ok { display: block; background: #dcfce7; color: #166534; }
    .signup-result.fail { display: block; background: #fee2e2; color: #991b1b; }
</style>

<div class="card">
    <h2>Реєстрація користувача</h2>
    <p>
        Дані форми надсилаються на <code>POST /user</code> у форматі JSON.
        Валідацію виконує модель <code>UserSignupFormModel</code>, а унікальність
        логіна та email перевіряє <code>UserDao</code>; помилки повертаються
        окремо для кожного поля (статус 447 Validation Failed).
        Доступність логіна / email перевіряється при виході з поля:
        <code>GET /user/available?login=...</code>.
    </p>

    <form id="signupForm" class="signup-form" novalidate>
        <div data-field="name">
            <label for="name">Ім'я *</label>
            <input type="text" id="name" name="name" />
            <div class="field-error"></div>
        </div>
        <div data-field="email">
            <label for="email">Email *</label>
            <input type="email" id="email" name="email" />
            <div class="field-error"></div>
        </div>
        <div data-field="login">
            <label for="login">Логін</label>
            <input type="text" id="login" name="login" />
            <div class="field-error"></div>
        </div>
        <div data-field="password">
            <label for="password">Пароль *</label>
            <input type="password" id="password" name="password" />
            <div class="field-error"></div>
        </div>
        <div data-field="repeatPassword">
            <label for="repeatPassword">Повтор пароля *</label>
            <input type="password" id="repeatPassword" name="repeatPassword" />
            <div class="field-error"></div>
        </div>
        <div data-field="agree">
            <label><input type="checkbox" id="agree" name="agree" /> Погоджуюсь з правилами сайту</label>
            <div class="field-error"></div>
        </div>
        <div>
            <button type="submit">Зареєструватись</button>
        </div>
    </form>

    <div id="signupResult" class="signup-result"></div>
</div>

<script>
// GET /user з Authorization: Basic base64(login:password) -> { token, user }
function signIn(login, password, result) {
    // btoa працює лише з Latin-1, тому спершу кодуємо у UTF-8 (кирилиця у паролі)
    const bytes = new TextEncoder().encode(login + ":" + password);
    const credentials = btoa(String.fromCharCode(...bytes));
    fetch("<%= contextPath %>/user", { headers: {
            "Authorization": "Basic " + credentials,
            "X-Requested-With": "fetch"     // без системного вікна логіну браузера при 401
        } })
        .then(r => r.json())
        .then(j => {
            if (j.status.isOk) {
                appAuth.set(j.data);
                result.className = "signup-result ok";
                result.innerText = "Реєстрація успішна. Вхід виконано: "
                    + j.data.user.name + " (" + j.data.user.login + ")";
            } else {
                result.className = "signup-result fail";
                result.innerText = "Реєстрація успішна, але вхід не вдався: " + j.status.code + " " + j.data;
            }
        });
}

// Перевірка доступності логіна / email "на льоту" (при виході з поля):
// GET /user/available?login=... -> { field, value, available }
for (const field of ["login", "email"]) {
    const input = document.getElementById(field);
    input.addEventListener("blur", () => {
        const value = input.value.trim();
        const box = input.closest("[data-field]");
        const msg = box.querySelector(".field-error");
        if (!value) { msg.innerText = ""; msg.classList.remove("ok"); box.classList.remove("valid", "invalid"); return; }
        fetch("<%= contextPath %>/user/available?" + field + "=" + encodeURIComponent(value))
            .then(r => r.json())
            .then(j => {
                if (!j.status.isOk) return;
                const free = j.data.available;
                msg.innerText = free
                    ? (field === "login" ? "Логін вільний" : "Email вільний")
                    : (field === "login" ? "Логін '" + value + "' вже зайнятий" : "Email '" + value + "' вже зареєстровано");
                msg.classList.toggle("ok", free);
                box.classList.toggle("valid", free);
                box.classList.toggle("invalid", !free);
            });
    });
}

document.getElementById("signupForm").addEventListener("submit", e => {
    e.preventDefault();
    const form = e.target;
    const data = {
        name:           form.name.value,
        email:          form.email.value,
        login:          form.login.value,
        password:       form.password.value,
        repeatPassword: form.repeatPassword.value,
        agree:          form.agree.checked
    };
    const result = document.getElementById("signupResult");

    fetch("<%= contextPath %>/user", {
        method: "POST",
        headers: { "Content-Type": "application/json; charset=UTF-8" },
        body: JSON.stringify(data)
    })
    .then(r => r.json())
    .then(j => {
        const errors = (j.status.code === 447 && j.data) ? j.data : {};
        for (const box of form.querySelectorAll("[data-field]")) {
            const msg = errors[box.dataset.field];
            box.querySelector(".field-error").innerText = msg || "";
            box.querySelector(".field-error").classList.remove("ok");
            box.classList.toggle("invalid", !!msg);
            box.classList.toggle("valid", !msg && j.status.code === 447);
        }
        if (j.status.isOk) {
            result.className = "signup-result ok";
            result.innerText = "Користувача зареєстровано: " + j.data.name + " <" + j.data.email + ">. Виконується вхід...";
            // вхід (автентифікація) з тими ж логіном і паролем, що введені у форму
            signIn(j.data.login, data.password, result);
        } else if (j.status.code === 447) {
            result.className = "signup-result fail";
            result.innerText = "Помилки валідації: " + Object.keys(errors).length;
        } else {
            result.className = "signup-result fail";
            result.innerText = j.status.code + " " + j.status.message + ": " + j.data;
        }
    })
    .catch(err => {
        result.className = "signup-result fail";
        result.innerText = err.toString();
    });
});
</script>

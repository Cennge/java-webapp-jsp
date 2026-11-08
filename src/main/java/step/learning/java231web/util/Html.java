package step.learning.java231web.util;

/**
 * Екранування тексту для виведення у HTML (вміст та значення атрибутів).
 * Усе, що походить від користувача (поля форм, дані з БД), виводиться через esc(),
 * інакше введене "&lt;script&gt;" виконається у браузері (XSS)
 * @author ilyag
 */
public final class Html {

    private Html() {
    }

    public static String esc(Object value) {
        if (value == null) {
            return "";
        }
        String s = value.toString();
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#39;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }
}

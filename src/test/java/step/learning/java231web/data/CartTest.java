package step.learning.java231web.data;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import step.learning.java231web.data.dto.Cart;
import step.learning.java231web.data.dto.CartItem;
import step.learning.java231web.models.AuthUser;

import static org.junit.jupiter.api.Assertions.*;

public class CartTest {

    private static CartItem item(String price, int qty) {
        return new CartItem().setProductId("p").setPrice(new BigDecimal(price)).setQuantity(qty);
    }

    @Test
    public void testOpenCartTotalIsSumOfItems() {
        Cart cart = new Cart().setStatus(Cart.OPEN).setItems(List.of(item("8999.50", 2), item("1499.90", 1)));
        assertEquals(0, new BigDecimal("19498.90").compareTo(cart.getTotal()));
        assertEquals(3, cart.getItemsCount());
        assertEquals("open", cart.getState());
    }

    @Test
    public void testClosedCartKeepsFixedPrice() {
        Cart cart = new Cart().setStatus(Cart.ORDERED).setPrice(new BigDecimal("100.00"))
                .setItems(List.of(item("999.00", 5)));
        assertEquals(0, new BigDecimal("100.00").compareTo(cart.getTotal()), "сума оформленого кошика не перераховується");
        assertEquals("ordered", cart.getState());
        assertEquals("dropped", new Cart().setStatus(Cart.DROPPED).getState());
    }

    @Test
    public void testAuthUserFromClaims() {
        AuthUser user = AuthUser.fromClaims(java.util.Map.of("sub", "42", "login", "illia", "role", "user"));
        assertEquals("42", user.id());
        assertEquals("illia", user.login());
        assertNull(user.email());
    }

    @Test
    public void testTokenOfHeaderAndQuery() throws Exception {
        var tokenOf = step.learning.java231web.filters.AuthFilter.class.getDeclaredMethod("tokenOf", HttpServletRequest.class);
        tokenOf.setAccessible(true);
        assertEquals("abc", tokenOf.invoke(null, request("Bearer abc", null)));
        assertEquals("abc", tokenOf.invoke(null, request("bearer  abc ", null)));
        assertEquals("x.y.z", tokenOf.invoke(null, request(null, "page=2&ua=x.y.z")));
        assertNull(tokenOf.invoke(null, request("Basic aaa", "ua=")));
        assertNull(tokenOf.invoke(null, request(null, null)));
    }

    private static HttpServletRequest request(String authorization, String query) {
        return (HttpServletRequest) Proxy.newProxyInstance(HttpServletRequest.class.getClassLoader(),
                new Class<?>[] { HttpServletRequest.class },
                (p, m, a) -> switch (m.getName()) {
                    case "getHeader" -> "Authorization".equals(a[0]) ? authorization : null;
                    case "getQueryString" -> query;
                    default -> null;
                });
    }
}

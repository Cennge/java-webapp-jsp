package step.learning.java231web.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import step.learning.java231web.data.DataContext;
import step.learning.java231web.data.dto.Cart;
import step.learning.java231web.data.dto.CartItem;
import step.learning.java231web.data.dto.Product;
import step.learning.java231web.data.dto.ProductVersion;
import step.learning.java231web.filters.AuthFilter;
import step.learning.java231web.models.AuthUser;
import step.learning.java231web.rest.RestResponse;
import step.learning.java231web.rest.RestServlet;
import step.learning.java231web.rest.RestStatus;
import step.learning.java231web.services.token.ITokenService;

/**
 * API 'Cart' (кошик автентифікованого користувача, токен - Authorization: Bearer або ?ua=):
 * GET    /cart                       - відкритий кошик (data: null, якщо його немає)
 * GET    /cart/ordered | /dropped | /history - оформлені / скасовані / усі закриті кошики
 * POST   /cart?pvid=...              - додати товар (id версії або id товару без версій)
 * PATCH  /cart?ciid=...&delta=+1|-1  - змінити кількість (або &cnt=N - встановити)
 * DELETE /cart?ciid=...              - видалити позицію
 * DELETE /cart                       - скасувати (закрити) відкритий кошик
 * POST   /cart/checkout (або DELETE /cart?checkout) - оформити замовлення
 * @author ilyag
 */
@Singleton
public class CartServlet extends RestServlet {
    private static final System.Logger LOGGER = System.getLogger(CartServlet.class.getName());

    private final DataContext dataContext;

    @Inject
    public CartServlet(DataContext dataContext) {
        this.dataContext = dataContext;
    }

    @Override
    protected String getServiceName() {
        return "API 'Cart'";
    }

    @Override
    protected String[] getManipulations() {
        return new String[] { "GET", "POST", "PATCH", "DELETE" };
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // HttpServlet не має doPatch - маршрутизуємо самі
        if ("PATCH".equalsIgnoreCase(req.getMethod())) {
            send(req, resp, handle(req, this::patch));
        } else {
            super.service(req, resp);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        send(req, resp, handle(req, this::get));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        send(req, resp, handle(req, (r, user) -> "checkout".equals(pathTail(r)) ? checkout(user) : add(r, user)));
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        send(req, resp, handle(req, (r, user) ->
                r.getParameter("checkout") != null ? checkout(user) : delete(r, user)));
    }

    // ------------------------------------------------------------ handlers

    private RestResponse get(HttpServletRequest req, AuthUser user) throws SQLException {
        String tail = pathTail(req);
        if (tail.isEmpty()) {
            return ok(req, dataContext.carts().getOpenCart(user.id()));
        }
        Integer status = switch (tail) {
            case "ordered" -> Cart.ORDERED;
            case "dropped" -> Cart.DROPPED;
            case "history" -> null;
            default -> -1;
        };
        if (status != null && status == -1) {
            return error(RestStatus.NotFound, "Unknown cart route '/" + tail + "'");
        }
        List<Cart> carts = dataContext.carts().getClosedCarts(user.id(), status);
        carts.forEach(c -> fixImages(req, c));
        return new RestResponse(RestStatus.Ok, carts);
    }

    /**
     * Додавання товару: 442 - немає pvid; 404 - товару немає;
     * 422 - товар видалено / немає на складі / треба обрати версію;
     * 208 - вже у кошику; 200 - кошик з доданим товаром
     */
    private RestResponse add(HttpServletRequest req, AuthUser user) throws SQLException {
        String pvid = req.getParameter("pvid");
        if (pvid == null || pvid.isBlank()) {
            return error(RestStatus.QueryParameterRequired, "'pvid' - product version id");
        }
        pvid = pvid.trim();
        ProductVersion version = dataContext.versions().getByIdIncludingDeleted(pvid);
        Product product = dataContext.products().getByIdIncludingDeleted(
                version != null ? version.getProductId() : pvid);
        if (product == null) {
            return error(RestStatus.NotFound, "Product '" + pvid + "' not found");
        }
        if (product.getDeletedAt() != null || (version != null && version.getDeletedAt() != null)) {
            return error(RestStatus.UnprocessableEntity, "Product deleted");
        }
        if (version == null && !dataContext.versions().getByProduct(product.getId()).isEmpty()) {
            return error(RestStatus.UnprocessableEntity, "Product has versions: pass version id as 'pvid'");
        }
        int stock = version != null ? version.getStock() : product.getStock();
        if (stock <= 0) {
            return error(RestStatus.UnprocessableEntity, "Product out of stock");
        }

        Cart cart = dataContext.carts().getOrCreateOpenCart(user.id());
        String versionId = version == null ? null : version.getId();
        if (dataContext.carts().findItem(cart, product.getId(), versionId) != null) {
            return new RestResponse(RestStatus.AlreadyReported, "Product is already in cart");
        }
        BigDecimal price = version != null ? version.getPrice() : product.getPrice();
        dataContext.carts().addItem(cart.getId(), product.getId(), versionId, price);
        return ok(req, dataContext.carts().getById(cart.getId()));
    }

    /**
     * Зміна кількості: 442 - немає ciid; 422 - немає / некоректний delta (cnt);
     * 404 - позиції немає; 403 - чужа позиція; 409 - позиція закритого кошика;
     * 200 з попередженням (рядок) - кількість стала б 0, від'ємною або перевищила б залишок;
     * 200 - оновлений кошик
     */
    private RestResponse patch(HttpServletRequest req, AuthUser user) throws SQLException {
        String ciid = req.getParameter("ciid");
        if (ciid == null || ciid.isBlank()) {
            return error(RestStatus.QueryParameterRequired, "'ciid' - cart item id");
        }
        String paramName = req.getParameter("delta") != null ? "delta"
                : req.getParameter("inc") != null ? "inc"
                : req.getParameter("cnt") != null ? "cnt" : null;
        Integer value = paramName == null ? null : parseInt(req.getParameter(paramName));
        if (value == null) {
            return error(RestStatus.UnprocessableEntity, "Missing or invalid 'delta' (or 'cnt') parameter");
        }
        RestResponse denied = checkOwnItem(user, ciid.trim());
        if (denied != null) {
            return denied;
        }
        CartItem item = dataContext.carts().getItemById(ciid.trim());
        int quantity = "cnt".equals(paramName) ? value : item.getQuantity() + value;
        if (quantity < 0) {
            return warning("'" + paramName + "' causes negative quantity");
        }
        if (quantity == 0) {
            return warning("'" + paramName + "' causes zero quantity. For deletion use DELETE request");
        }
        int stock = item.getProductVersionId() != null
                ? dataContext.versions().getByIdIncludingDeleted(item.getProductVersionId()).getStock()
                : dataContext.products().getByIdIncludingDeleted(item.getProductId()).getStock();
        if (quantity > stock) {
            return warning("'" + paramName + "' exceeded stock limit (" + stock + ")");
        }
        dataContext.carts().setItemQuantity(item.getId(), quantity);
        return ok(req, dataContext.carts().getById(item.getCartId()));
    }

    /**
     * DELETE /cart?ciid=... - видалення позиції, перевірки по черзі:
     * 404 - позиції немає у БД; 403 - позиція іншого користувача;
     * 422 - у користувача немає відкритого кошика; 409 - позиція належить закритому кошику;
     * 200 - оновлений кошик.
     * DELETE /cart - скасування відкритого кошика (404, якщо його немає)
     */
    private RestResponse delete(HttpServletRequest req, AuthUser user) throws SQLException {
        String ciid = req.getParameter("ciid");
        if (ciid == null) {
            Cart cart = dataContext.carts().getOpenCart(user.id());
            if (cart == null) {
                return error(RestStatus.NotFound, "User has no open cart");
            }
            dataContext.carts().close(cart, Cart.DROPPED);
            return ok(req, dataContext.carts().getById(cart.getId()));
        }
        if (ciid.isBlank()) {
            return error(RestStatus.QueryParameterRequired, "'ciid' - cart item id");
        }
        RestResponse denied = checkOwnItem(user, ciid.trim());
        if (denied != null) {
            return denied;
        }
        CartItem item = dataContext.carts().getItemById(ciid.trim());
        dataContext.carts().deleteItem(item.getId());
        return ok(req, dataContext.carts().getById(item.getCartId()));
    }

    /** Оформлення: 404 - немає відкритого кошика; 422 - кошик порожній або товару не вистачає */
    private RestResponse checkout(AuthUser user) throws SQLException {
        Cart cart = dataContext.carts().getOpenCart(user.id());
        if (cart == null) {
            return error(RestStatus.NotFound, "User has no open cart");
        }
        if (cart.getItems().isEmpty()) {
            return error(RestStatus.UnprocessableEntity, "Cart is empty");
        }
        for (CartItem item : cart.getItems()) {
            int stock = item.getProductVersionId() != null
                    ? dataContext.versions().getByIdIncludingDeleted(item.getProductVersionId()).getStock()
                    : dataContext.products().getByIdIncludingDeleted(item.getProductId()).getStock();
            if (item.getQuantity() > stock) {
                return error(RestStatus.UnprocessableEntity, "Not enough stock for '" + item.getTitle() + "' (" + stock + ")");
            }
        }
        dataContext.carts().close(cart, Cart.ORDERED);
        return new RestResponse(RestStatus.Ok, dataContext.carts().getById(cart.getId()));
    }

    /**
     * Спільні перевірки позиції для PATCH / DELETE.
     * Д.З. Спочатку шукаємо ciid у відкритому кошику користувача. Якщо там його немає -
     * перевіряємо, чи існує такий запис у БД взагалі, і повідомляємо, кому він належить:
     * 404 - запису немає у БД;
     * 403 - запис належить кошику іншого користувача;
     * 422 - запис з власного закритого кошика, а відкритого кошика немає;
     * 409 - запис належить власному закритому (ordered / dropped) кошику
     */
    private RestResponse checkOwnItem(AuthUser user, String ciid) throws SQLException {
        Cart open = dataContext.carts().getOpenCart(user.id());
        if (open != null && open.getItems().stream().anyMatch(i -> ciid.equals(i.getId()))) {
            return null;  // позиція у відкритому кошику користувача - все гаразд
        }
        // у кошику користувача такої позиції немає - з'ясовуємо, чи існує вона взагалі
        CartItem item = dataContext.carts().getItemById(ciid);
        if (item == null) {
            return error(RestStatus.NotFound, "Cart item '" + ciid + "' not found: no such record in DB");
        }
        Cart itemCart = dataContext.carts().getById(item.getCartId());
        if (itemCart == null || !user.id().equals(itemCart.getUserId())) {
            return error(RestStatus.Forbidden, "Cart item '" + ciid + "' exists but belongs to another user's cart");
        }
        String owner = "your " + itemCart.getState() + " cart '" + itemCart.getId() + "'"
                + (itemCart.getClosedAt() == null ? "" : " closed at " + itemCart.getClosedAt().toInstant());
        if (open == null) {
            return error(RestStatus.UnprocessableEntity,
                    "User has no open cart: cart item '" + ciid + "' belongs to " + owner);
        }
        return error(RestStatus.Conflict, "Cart item '" + ciid + "' is not in open cart: it belongs to " + owner);
    }

    // ------------------------------------------------------------ helpers

    @FunctionalInterface
    private interface Handler {
        RestResponse handle(HttpServletRequest req, AuthUser user) throws SQLException;
    }

    /** Перевірка автентифікації (401 з точним кодом 444/445/446), установка БД, обробка помилок */
    private RestResponse handle(HttpServletRequest req, Handler handler) {
        AuthUser user = (AuthUser) req.getAttribute(AuthUser.ATTRIBUTE);
        if (user == null) {
            Object error = req.getAttribute(AuthFilter.ERROR_ATTRIBUTE);
            if (error instanceof ITokenService.TokenException ex) {
                return error(ex.isExpired() ? RestStatus.TokenExpired : RestStatus.TokenMalformed, ex.getMessage());
            }
            return error(RestStatus.TokenRequired, "Authorization: Bearer token (or ?ua=token) required");
        }
        try {
            dataContext.ensureInstalled();
            return handler.handle(req, user);
        } catch (SQLException ex) {
            LOGGER.log(System.Logger.Level.ERROR, "cart " + req.getMethod(), ex);
            return error(RestStatus.DatabaseError, "Cart is temporarily unavailable");
        }
    }

    private RestResponse ok(HttpServletRequest req, Cart cart) {
        if (cart != null) {
            fixImages(req, cart);
        }
        RestResponse restResponse = new RestResponse(RestStatus.Ok, cart);
        restResponse.getMeta()
                .addLink("base", req.getContextPath() + "/cart")
                .addLink("history", req.getContextPath() + "/cart/history")
                .addLink("checkout", req.getContextPath() + "/cart/checkout");
        return restResponse;
    }

    private RestResponse warning(String message) {
        return new RestResponse(RestStatus.Ok, message);
    }

    private static void fixImages(HttpServletRequest req, Cart cart) {
        for (CartItem item : cart.getItems()) {
            item.setImageUrl(storageUrl(req, item.getImageUrl()));
        }
    }

    private static Integer parseInt(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim().replace("+", ""));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}

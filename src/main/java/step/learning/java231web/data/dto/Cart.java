package step.learning.java231web.data.dto;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Кошик користувача. У кожного користувача не більше одного відкритого кошика;
 * закритий кошик - оформлений (ordered) або скасований (dropped)
 * @author ilyag
 */
public class Cart {
    public static final int OPEN = 0;
    public static final int ORDERED = 1;
    public static final int DROPPED = 2;

    private String id;
    private String userId;
    private int status;
    private Date openedAt;
    private Date closedAt;
    private BigDecimal price = BigDecimal.ZERO;   // зафіксована сума закритого кошика
    private List<CartItem> items = new ArrayList<>();
    // обчислювані значення - полями, бо Gson серіалізує поля, а не get-методи
    private String state = "open";
    private BigDecimal total = BigDecimal.ZERO;
    private int itemsCount;

    /** Перерахунок state / total / itemsCount після зміни стану, суми чи позицій */
    private Cart refresh() {
        state = switch (status) {
            case OPEN -> "open";
            case ORDERED -> "ordered";
            case DROPPED -> "dropped";
            default -> "unknown";
        };
        total = isOpen()
                ? items.stream().map(CartItem::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add)
                : price;
        itemsCount = items.stream().mapToInt(CartItem::getQuantity).sum();
        return this;
    }

    public static Cart fromResultSet(ResultSet rs) throws SQLException {
        Timestamp opened = rs.getTimestamp("cart_open_dt");
        Timestamp closed = rs.getTimestamp("cart_close_dt");
        return new Cart()
                .setId(rs.getString("cart_id"))
                .setUserId(rs.getString("user_id"))
                .setStatus(rs.getInt("cart_status"))
                .setOpenedAt(opened == null ? null : new Date(opened.getTime()))
                .setClosedAt(closed == null ? null : new Date(closed.getTime()))
                .setPrice(rs.getBigDecimal("cart_price"));
    }

    public boolean isOpen() {
        return status == OPEN;
    }

    /** Назва стану: open / ordered / dropped */
    public String getState() {
        return state;
    }

    /** Поточна сума: для відкритого кошика - за позиціями, для закритого - зафіксована */
    public BigDecimal getTotal() {
        return total;
    }

    public int getItemsCount() {
        return itemsCount;
    }

    public String getId() {
        return id;
    }

    public Cart setId(String id) {
        this.id = id;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public Cart setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public int getStatus() {
        return status;
    }

    public Cart setStatus(int status) {
        this.status = status;
        return refresh();
    }

    public Date getOpenedAt() {
        return openedAt;
    }

    public Cart setOpenedAt(Date openedAt) {
        this.openedAt = openedAt;
        return this;
    }

    public Date getClosedAt() {
        return closedAt;
    }

    public Cart setClosedAt(Date closedAt) {
        this.closedAt = closedAt;
        return this;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Cart setPrice(BigDecimal price) {
        this.price = price != null ? price : BigDecimal.ZERO;
        return refresh();
    }

    public List<CartItem> getItems() {
        return items;
    }

    public Cart setItems(List<CartItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        return refresh();
    }
}

package step.learning.java231web.rest;

/**
 * Пагінація колекцій: номер сторінки (з 1), розмір сторінки,
 * загальна кількість елементів та сторінок
 * @author ilyag
 */
public class RestPagination {
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;

    private int page;
    private int pageSize;
    private int totalPages;
    private int totalItems;

    public RestPagination() {
        this(1, DEFAULT_PAGE_SIZE, 0);
    }

    public RestPagination(int page, int pageSize, int totalItems) {
        this.page = Math.max(1, page);
        this.pageSize = pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE;
        setTotalItems(totalItems);
    }

    /** Зміщення першого елемента сторінки (для SQL OFFSET) */
    public int getOffset() {
        return (page - 1) * pageSize;
    }

    public int getPage() {
        return page;
    }

    public RestPagination setPage(int page) {
        this.page = Math.max(1, page);
        return this;
    }

    public int getPageSize() {
        return pageSize;
    }

    public RestPagination setPageSize(int pageSize) {
        this.pageSize = pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE;
        updateTotalPages();
        return this;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public RestPagination setTotalItems(int totalItems) {
        this.totalItems = Math.max(0, totalItems);
        updateTotalPages();
        return this;
    }

    public int getTotalPages() {
        return totalPages;
    }

    private void updateTotalPages() {
        // порожня колекція - одна (порожня) сторінка
        this.totalPages = Math.max(1, (totalItems + pageSize - 1) / pageSize);
    }
}

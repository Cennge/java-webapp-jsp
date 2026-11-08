package step.learning.java231web.rest;

/**
 * Стандартна REST-відповідь: { status, meta, data }.
 * Пагінація - частина метаданих (meta.pagination)
 * @author ilyag
 */
public class RestResponse {
    private RestStatus status;
    private RestMeta meta;
    private Object data;

    public RestResponse() {
        this(RestStatus.Ok, null);
    }

    public RestResponse(RestStatus status) {
        this(status, null);
    }

    public RestResponse(RestStatus status, Object data) {
        this.status = status;
        this.data = data;
        this.meta = new RestMeta();
    }

    public RestStatus getStatus() {
        return status;
    }

    public RestResponse setStatus(RestStatus status) {
        this.status = status;
        return this;
    }

    public RestMeta getMeta() {
        return meta;
    }

    public RestResponse setMeta(RestMeta meta) {
        this.meta = meta;
        return this;
    }

    public RestPagination getPagination() {
        return meta == null ? null : meta.getPagination();
    }

    public RestResponse setPagination(RestPagination pagination) {
        if (meta == null) {
            meta = new RestMeta();
        }
        meta.setPagination(pagination);
        return this;
    }

    public Object getData() {
        return data;
    }

    public RestResponse setData(Object data) {
        this.data = data;
        return this;
    }
}

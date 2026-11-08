package step.learning.java231web.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Метадані REST-відповіді (стандарт RestResponse):
 * serviceName  - назва сервісу (API), що сформував відповідь
 * dataType     - тип поля data: object, array, string, number, boolean, null
 * serverTime   - час сервера на момент формування відповіді (мс)
 * cacheTime    - скільки секунд відповідь можна кешувати (0 - не кешувати)
 * manipulations- HTTP-методи, які підтримує ресурс
 * links        - пов'язані ресурси (HATEOAS): rel -> url
 * parameters   - параметри, з якими виконано запит (slug, page...)
 * pagination   - пагінація для колекцій
 * url, method  - на який саме запит відповідь
 * @author ilyag
 */
public class RestMeta {
    private String serviceName;
    private String url;
    private String method;
    private String dataType;
    private long serverTime;
    private long cacheTime;
    private List<String> manipulations;
    private Map<String, String> links;
    private Map<String, Object> parameters;
    private RestPagination pagination;

    public RestMeta() {
        this.serverTime = System.currentTimeMillis();
    }

    public RestMeta(String serviceName, String dataType) {
        this();
        this.serviceName = serviceName;
        this.dataType = dataType;
    }

    /** Тип даних для поля dataType */
    public static String typeOf(Object data) {
        if (data == null) return "null";
        if (data instanceof CharSequence) return "string";
        if (data instanceof Number) return "number";
        if (data instanceof Boolean) return "boolean";
        if (data instanceof Collection || data.getClass().isArray()) return "array";
        return "object";
    }

    public RestMeta addLink(String rel, String href) {
        if (links == null) {
            links = new LinkedHashMap<>();
        }
        links.put(rel, href);
        return this;
    }

    public RestMeta addParameter(String name, Object value) {
        if (parameters == null) {
            parameters = new LinkedHashMap<>();
        }
        parameters.put(name, value);
        return this;
    }

    public RestMeta setManipulations(String... methods) {
        this.manipulations = new ArrayList<>(List.of(methods));
        return this;
    }

    public String getServiceName() {
        return serviceName;
    }

    public RestMeta setServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public RestMeta setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getMethod() {
        return method;
    }

    public RestMeta setMethod(String method) {
        this.method = method;
        return this;
    }

    public String getDataType() {
        return dataType;
    }

    public RestMeta setDataType(String dataType) {
        this.dataType = dataType;
        return this;
    }

    public long getServerTime() {
        return serverTime;
    }

    public RestMeta setServerTime(long serverTime) {
        this.serverTime = serverTime;
        return this;
    }

    public long getCacheTime() {
        return cacheTime;
    }

    public RestMeta setCacheTime(long cacheTime) {
        this.cacheTime = cacheTime;
        return this;
    }

    public List<String> getManipulations() {
        return manipulations;
    }

    public RestMeta setManipulations(List<String> manipulations) {
        this.manipulations = manipulations;
        return this;
    }

    public Map<String, String> getLinks() {
        return links;
    }

    public RestMeta setLinks(Map<String, String> links) {
        this.links = links;
        return this;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public RestMeta setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
        return this;
    }

    public RestPagination getPagination() {
        return pagination;
    }

    public RestMeta setPagination(RestPagination pagination) {
        this.pagination = pagination;
        return this;
    }
}

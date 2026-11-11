package step.learning.java231web.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import step.learning.java231web.data.DataContext;
import step.learning.java231web.data.dto.Product;
import step.learning.java231web.rest.RestPagination;
import step.learning.java231web.rest.RestResponse;
import step.learning.java231web.rest.RestServlet;
import step.learning.java231web.rest.RestStatus;
import step.learning.java231web.util.Views;

/**
 * API 'Products':
 * GET /product?page=1&pageSize=10 - сторінка товарів
 * GET /product/{id або slug}       - товар з версіями (JSON)
 * GET /product/{slug} з браузера (Accept: text/html) - сторінка товару product.jsp
 * @author ilyag
 */
@Singleton
public class ProductServlet extends RestServlet {
    private static final System.Logger LOGGER = System.getLogger(ProductServlet.class.getName());

    private final DataContext dataContext;

    @Inject
    public ProductServlet(DataContext dataContext) {
        this.dataContext = dataContext;
    }

    @Override
    protected String getServiceName() {
        return "API 'Products'";
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idOrSlug = pathTail(req);
        if (!idOrSlug.isEmpty() && wantsHtml(req)) {
            productPage(req, resp, idOrSlug);
            return;
        }
        send(req, resp, handle(req));
    }

    /** Браузер просить HTML, API-клієнти (fetch, curl) - JSON; ?json примусово дає JSON */
    private static boolean wantsHtml(HttpServletRequest req) {
        String accept = req.getHeader("Accept");
        return req.getParameter("json") == null && accept != null && accept.contains("text/html");
    }

    /**
     * Д.З. Сторінка товару: якщо версій декілька - спільна назва одним заголовком,
     * версії - лише додатками (Addon); якщо версія одна (або немає) - структура без змін
     */
    private void productPage(HttpServletRequest req, HttpServletResponse resp, String slug)
            throws ServletException, IOException {
        try {
            dataContext.ensureInstalled();
            Product product = dataContext.products().getByIdOrSlug(slug);
            if (product == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                req.setAttribute("notFound", slug);
            } else {
                product.setVersions(dataContext.versions().getByProduct(product.getId()));
                ProductSectionServlet.fixImages(req, product);
                req.setAttribute("product", product);
            }
        } catch (Exception ex) {
            LOGGER.log(System.Logger.Level.ERROR, "product page " + slug, ex);
            req.setAttribute("dbError", "Каталог тимчасово недоступний");
        }
        req.setAttribute("servlet", "Product");
        Views.layout(req, resp);
    }

    private RestResponse handle(HttpServletRequest req) {
        String idOrSlug = pathTail(req);
        try {
            dataContext.ensureInstalled();
            RestResponse restResponse = idOrSlug.isEmpty() ? productsList(req) : productDetails(req, idOrSlug);
            if (restResponse.getStatus().isOk()) {
                restResponse.getMeta()
                        .addLink("list", req.getContextPath() + "/product")
                        .addLink("details", req.getContextPath() + "/product/{slug}")
                        .setCacheTime(60);
            }
            return restResponse;
        } catch (QueryParameterException ex) {
            return error(RestStatus.QueryParameterMalformed, ex.getMessage());
        } catch (Exception ex) {
            LOGGER.log(System.Logger.Level.ERROR, "product " + idOrSlug, ex);
            return error(RestStatus.DatabaseError, "Catalog is temporarily unavailable");
        }
    }

    private RestResponse productsList(HttpServletRequest req) throws Exception {
        RestPagination pagination = paginationOf(req, RestPagination.DEFAULT_PAGE_SIZE);
        pagination.setTotalItems(dataContext.products().count());
        List<Product> products = dataContext.products().getPage(pagination);
        products.forEach(p -> ProductSectionServlet.fixImages(req, p));
        RestResponse restResponse = new RestResponse(RestStatus.Ok, products).setPagination(pagination);
        restResponse.getMeta()
                .addParameter("page", pagination.getPage())
                .addParameter("pageSize", pagination.getPageSize());
        return restResponse;
    }

    private RestResponse productDetails(HttpServletRequest req, String idOrSlug) throws Exception {
        Product product = dataContext.products().getByIdOrSlug(idOrSlug);
        if (product == null) {
            return new RestResponse(RestStatus.NotFound.withMessage("Product '" + idOrSlug + "' not found"), null);
        }
        product.setVersions(dataContext.versions().getByProduct(product.getId()));
        ProductSectionServlet.fixImages(req, product);
        RestResponse restResponse = new RestResponse(RestStatus.Ok, product);
        restResponse.getMeta().addParameter("slug", idOrSlug);
        return restResponse;
    }
}

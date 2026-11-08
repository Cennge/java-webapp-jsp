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
import step.learning.java231web.data.dto.ProductSection;
import step.learning.java231web.data.dto.ProductVersion;
import step.learning.java231web.rest.RestPagination;
import step.learning.java231web.rest.RestResponse;
import step.learning.java231web.rest.RestServlet;
import step.learning.java231web.rest.RestStatus;

/**
 * API 'Product Sections':
 * GET /section?page=1&pageSize=10       - кореневі розділи з підрозділами (children), пагінація - по кореневих
 * GET /section/{slug}?page=1&pageSize=10 - розділ з підрозділами та сторінкою товарів (з версіями)
 * Поля imageUrl - абсолютні адреси файлів сховища (/storage/{file})
 * @author ilyag
 */
@Singleton
public class ProductSectionServlet extends RestServlet {
    private static final System.Logger LOGGER = System.getLogger(ProductSectionServlet.class.getName());

    private final DataContext dataContext;

    @Inject
    public ProductSectionServlet(DataContext dataContext) {
        this.dataContext = dataContext;
    }

    @Override
    protected String getServiceName() {
        return "API 'Product Sections'";
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        send(req, resp, handle(req));
    }

    private RestResponse handle(HttpServletRequest req) {
        RestPagination pagination;
        try {
            pagination = paginationOf(req, RestPagination.DEFAULT_PAGE_SIZE);
        } catch (QueryParameterException ex) {
            return error(RestStatus.QueryParameterMalformed, ex.getMessage());
        }
        String slug = pathTail(req);
        try {
            dataContext.ensureInstalled();
            RestResponse restResponse = slug.isEmpty()
                    ? sectionsList(req, pagination)
                    : sectionDetails(req, slug, pagination);
            if (restResponse.getStatus().isOk()) {
                restResponse.getMeta()
                        .addLink("list", req.getContextPath() + "/section")
                        .addLink("details", req.getContextPath() + "/section/{slug}")
                        .addParameter("page", pagination.getPage())
                        .addParameter("pageSize", pagination.getPageSize())
                        .setCacheTime(60);
            }
            return restResponse;
        } catch (Exception ex) {
            LOGGER.log(System.Logger.Level.ERROR, "section " + slug, ex);
            return error(RestStatus.DatabaseError, "Catalog is temporarily unavailable");
        }
    }

    private RestResponse sectionsList(HttpServletRequest req, RestPagination pagination) throws Exception {
        List<ProductSection> roots = dataContext.sections().getTree();
        pagination.setTotalItems(roots.size());
        int from = Math.min(pagination.getOffset(), roots.size());
        int to = Math.min(from + pagination.getPageSize(), roots.size());
        List<ProductSection> page = roots.subList(from, to);
        page.forEach(s -> fixImages(req, s));
        return new RestResponse(RestStatus.Ok, page).setPagination(pagination);
    }

    private RestResponse sectionDetails(HttpServletRequest req, String slug, RestPagination pagination) throws Exception {
        ProductSection section = dataContext.sections().getBySlug(slug);
        if (section == null) {
            return new RestResponse(RestStatus.NotFound.withMessage("Section '" + slug + "' not found"), null);
        }
        section.setChildren(dataContext.sections().getChildren(section.getId()));
        pagination.setTotalItems(dataContext.products().countBySection(section.getId()));
        List<Product> products = dataContext.products().getBySection(section.getId(), pagination);
        for (Product product : products) {
            product.setVersions(dataContext.versions().getByProduct(product.getId()));
        }
        section.setProducts(products);
        fixImages(req, section);

        RestResponse restResponse = new RestResponse(RestStatus.Ok, section).setPagination(pagination);
        restResponse.getMeta().addParameter("slug", slug);
        return restResponse;
    }

    /**
     * У БД - лише імена файлів; у відповіді - абсолютні адреси сховища.
     * Коригуються розділ, його підрозділи, товари та версії
     * (версія без власного зображення отримує зображення товару)
     */
    private void fixImages(HttpServletRequest req, ProductSection section) {
        section.setImageUrl(storageUrl(req, section.getImageUrl()));
        for (ProductSection child : section.getChildren()) {
            fixImages(req, child);
        }
        for (Product product : section.getProducts()) {
            fixImages(req, product);
        }
    }

    static void fixImages(HttpServletRequest req, Product product) {
        product.setImageUrl(storageUrl(req, product.getImageUrl()));
        for (ProductVersion version : product.getVersions()) {
            version.setImageUrl(version.getImageUrl() != null
                    ? storageUrl(req, version.getImageUrl())
                    : product.getImageUrl());
        }
    }
}

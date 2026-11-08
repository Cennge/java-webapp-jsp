package step.learning.java231web.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import step.learning.java231web.data.DataContext;
import step.learning.java231web.data.dao.ProductSectionDao;
import step.learning.java231web.data.dto.ProductSection;
import step.learning.java231web.rest.RestPagination;
import step.learning.java231web.util.Views;

/**
 * Вітрина каталогу:
 * GET /catalog         - основні товарні групи (кореневі розділи) з підгрупами
 * GET /catalog/{slug}  - товарна група: шлях (хлібні крихти), картки підгруп, товари
 * @author ilyag
 */
@Singleton
public class CatalogServlet extends HttpServlet {
    private static final System.Logger LOGGER = System.getLogger(CatalogServlet.class.getName());
    private static final int PRODUCTS_PAGE_SIZE = 12;

    private final DataContext dataContext;

    @Inject
    public CatalogServlet(DataContext dataContext) {
        this.dataContext = dataContext;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String slug = req.getPathInfo() == null ? "" : req.getPathInfo().replaceAll("^/+|/+$", "");
        try {
            dataContext.ensureInstalled();
            List<ProductSection> all = dataContext.sections().getAll();
            if (slug.isEmpty()) {
                req.setAttribute("groups", ProductSectionDao.buildTree(all));
            } else {
                Map<String, ProductSection> byId = all.stream()
                        .collect(Collectors.toMap(ProductSection::getId, Function.identity()));
                ProductSection section = all.stream().filter(s -> slug.equals(s.getSlug())).findFirst().orElse(null);
                if (section == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    req.setAttribute("notFound", slug);
                } else {
                    ProductSectionDao.buildTree(all);   // заповнює children у всіх розділів
                    RestPagination pagination = new RestPagination(page(req), PRODUCTS_PAGE_SIZE,
                            dataContext.products().countBySection(section.getId()));
                    section.setProducts(dataContext.products().getBySection(section.getId(), pagination));
                    req.setAttribute("section", section);
                    req.setAttribute("breadcrumbs", breadcrumbs(section, byId));
                    req.setAttribute("pagination", pagination);
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(System.Logger.Level.ERROR, "catalog " + slug, ex);
            req.setAttribute("dbError", "Каталог тимчасово недоступний");
        }
        req.setAttribute("servlet", "Catalog");
        Views.layout(req, resp);
    }

    /** Батьківські розділи від кореня до безпосереднього батька */
    private static List<ProductSection> breadcrumbs(ProductSection section, Map<String, ProductSection> byId) {
        List<ProductSection> path = new ArrayList<>();
        ProductSection current = byId.get(section.getParentId());
        while (current != null && !path.contains(current)) {   // захист від циклу в даних
            path.add(current);
            current = byId.get(current.getParentId());
        }
        Collections.reverse(path);
        return path;
    }

    private static int page(HttpServletRequest req) {
        try {
            return Math.max(1, Integer.parseInt(req.getParameter("page")));
        } catch (NumberFormatException ex) {
            return 1;
        }
    }
}

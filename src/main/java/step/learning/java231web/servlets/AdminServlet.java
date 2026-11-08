package step.learning.java231web.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.fileupload2.core.FileItem;
import step.learning.java231web.data.DataContext;
import step.learning.java231web.data.dto.Product;
import step.learning.java231web.data.dto.ProductSection;
import step.learning.java231web.data.dto.ProductVersion;
import step.learning.java231web.models.FormValidation;
import step.learning.java231web.models.ProductFormModel;
import step.learning.java231web.models.ProductVersionFormModel;
import step.learning.java231web.models.SectionFormModel;
import step.learning.java231web.services.form.FormParseResult;
import step.learning.java231web.services.form.IFormParseService;
import step.learning.java231web.services.storage.IStorageService;
import step.learning.java231web.util.Views;

/**
 * Адміністративна сторінка з внутрішніми маршрутами:
 * GET  /admin/, /admin/home         - головна панель (стан каталогу)
 * GET  /admin/section               - розділи: перелік + форма додавання
 * GET  /admin/product               - товари: перелік + форми товару та версії
 * POST /admin/section               - додати розділ        (multipart/form-data)
 * POST /admin/product               - додати товар         (multipart/form-data)
 * POST /admin/product-version       - додати версію товару (multipart/form-data)
 * інші внутрішні маршрути           - сторінка 404 адмінки
 *
 * Після успішного додавання - перенаправлення (Post/Redirect/Get), щоб оновлення
 * сторінки не надсилало форму повторно; повідомлення передається через сесію
 * @author ilyag
 */
@Singleton
public class AdminServlet extends HttpServlet {
    private static final System.Logger LOGGER = System.getLogger(AdminServlet.class.getName());
    private static final String FLASH = "adminFlash";

    private final DataContext dataContext;
    private final IFormParseService formParseService;
    private final IStorageService storageService;

    @Inject
    public AdminServlet(DataContext dataContext, IFormParseService formParseService, IStorageService storageService) {
        this.dataContext = dataContext;
        this.formParseService = formParseService;
        this.storageService = storageService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String route = route(req);
        switch (route) {
            case "", "home" -> render(req, resp, "home");
            case "section"  -> render(req, resp, "section");
            case "product"  -> render(req, resp, "product");
            default         -> notFound(req, resp, route);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String route = route(req);
        switch (route) {
            case "section"         -> addSection(req, resp);
            case "product"         -> addProduct(req, resp);
            case "product-version" -> addVersion(req, resp);
            default                -> notFound(req, resp, route);
        }
    }

    /** Внутрішній маршрут: /admin/product/ -> "product" */
    private static String route(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            return "";
        }
        return pathInfo.replaceAll("^/+|/+$", "").toLowerCase();
    }

    // ------------------------------------------------------------ POST handlers

    private void addSection(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        FormParseResult form = parseForm(req);
        if (form == null) {
            render(req, resp, "section");
            return;
        }
        SectionFormModel model = SectionFormModel.fromForm(form);
        Map<String, String> errors = new LinkedHashMap<>(model.getValidationErrors());
        try {
            dataContext.ensureInstalled();
            if (model.getParentId() != null && !dataContext.sections().existsById(model.getParentId())) {
                errors.putIfAbsent("section-parent-id", "Обраний батьківський розділ не існує");
            }
            if (!errors.containsKey("section-slug") && !dataContext.sections().isSlugAvailable(model.getSlug().trim())) {
                errors.put("section-slug", "Слаг '" + model.getSlug().trim() + "' вже використовується");
            }
            if (errors.isEmpty()) {
                ProductSection section = new ProductSection()
                        .setParentId(model.getParentId())
                        .setTitle(model.getTitle().trim())
                        .setDescription(model.getDescription().trim())
                        .setSlug(model.getSlug().trim())
                        .setOrderInPrice(Integer.parseInt(model.getOrder().trim()))
                        .setImageUrl(storageService.save(model.getImageItem()));
                dataContext.sections().add(section);
                redirectWithFlash(req, resp, "section",
                        "Розділ «" + section.getTitle() + "» додано" + fileNote(model.getImageItem(), section.getImageUrl()));
                return;
            }
        } catch (SQLException | IOException ex) {
            LOGGER.log(System.Logger.Level.ERROR, "addSection", ex);
            req.setAttribute("formError", "Не вдалося зберегти розділ: " + friendly(ex));
        }
        rejectForm(req, form, "section", errors);
        render(req, resp, "section");
    }

    private void addProduct(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        FormParseResult form = parseForm(req);
        if (form == null) {
            render(req, resp, "product");
            return;
        }
        ProductFormModel model = ProductFormModel.fromForm(form);
        Map<String, String> errors = new LinkedHashMap<>(model.getValidationErrors());
        try {
            dataContext.ensureInstalled();
            if (model.getSectionId() != null && !dataContext.sections().existsById(model.getSectionId())) {
                errors.putIfAbsent("product-section-id", "Обраний розділ не існує");
            }
            if (!errors.containsKey("product-slug") && !dataContext.products().isSlugAvailable(model.getSlug().trim())) {
                errors.put("product-slug", "Слаг '" + model.getSlug().trim() + "' вже використовується");
            }
            if (errors.isEmpty()) {
                Product product = new Product()
                        .setSectionId(model.getSectionId())
                        .setTitle(model.getTitle().trim())
                        .setDescription(model.getDescription().trim())
                        .setSlug(model.getSlug().trim())
                        .setPrice(FormValidation.parsePrice(model.getPrice()))
                        .setStock(Integer.parseInt(model.getStock().trim()))
                        .setOrderInPrice(Integer.parseInt(model.getOrder().trim()))
                        .setImageUrl(storageService.save(model.getImageItem()));
                dataContext.products().add(product);
                redirectWithFlash(req, resp, "product",
                        "Товар «" + product.getTitle() + "» додано" + fileNote(model.getImageItem(), product.getImageUrl()));
                return;
            }
        } catch (SQLException | IOException ex) {
            LOGGER.log(System.Logger.Level.ERROR, "addProduct", ex);
            req.setAttribute("formError", "Не вдалося зберегти товар: " + friendly(ex));
        }
        rejectForm(req, form, "product", errors);
        render(req, resp, "product");
    }

    private void addVersion(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        FormParseResult form = parseForm(req);
        if (form == null) {
            req.setAttribute("formType", "version");
            render(req, resp, "product");
            return;
        }
        ProductVersionFormModel model = ProductVersionFormModel.fromForm(form);
        Map<String, String> errors = new LinkedHashMap<>(model.getValidationErrors());
        try {
            dataContext.ensureInstalled();
            Product product = model.getProductId() == null ? null
                    : dataContext.products().getByIdOrSlug(model.getProductId());
            if (model.getProductId() != null && product == null) {
                errors.putIfAbsent("product-id", "Обраний товар не існує");
            }
            if (product != null && !errors.containsKey("version-slug")
                    && !dataContext.versions().isSlugAddonAvailable(product.getId(), model.getSlugAddon().trim())) {
                errors.put("version-slug", "Версія зі слагом '" + model.getSlugAddon().trim() + "' у цього товару вже є");
            }
            if (errors.isEmpty()) {
                FileItem<?> image = model.getImageItem();
                ProductVersion version = new ProductVersion()
                        .setProductId(product.getId())
                        .setTitleAddon(model.getTitleAddon().trim())
                        .setDescriptionAddon(model.getDescriptionAddon().trim())
                        .setSlugAddon(model.getSlugAddon().trim())
                        .setPrice(FormValidation.parsePrice(model.getPrice()))
                        .setStock(Integer.parseInt(model.getStock().trim()))
                        .setOrderInPrice(Integer.parseInt(model.getOrder().trim()))
                        .setImageUrl(image == null ? null : storageService.save(image));
                dataContext.versions().add(version);
                redirectWithFlash(req, resp, "product",
                        "Версію «" + product.getTitle() + " " + version.getTitleAddon() + "» додано"
                        + fileNote(image, version.getImageUrl()));
                return;
            }
        } catch (SQLException | IOException ex) {
            LOGGER.log(System.Logger.Level.ERROR, "addVersion", ex);
            req.setAttribute("formError", "Не вдалося зберегти версію: " + friendly(ex));
        }
        rejectForm(req, form, "version", errors);
        render(req, resp, "product");
    }

    // ------------------------------------------------------------ helpers

    /** Розбір форми; при помилці (напр. завеликий файл) - null та повідомлення formError */
    private FormParseResult parseForm(HttpServletRequest req) {
        try {
            return formParseService.parse(req);
        } catch (IFormParseService.FormParseException ex) {
            req.setAttribute("formError", ex.getMessage());
            return null;
        }
    }

    /** Повернення форми з помилками: введені значення лишаються у полях */
    private void rejectForm(HttpServletRequest req, FormParseResult form, String formType, Map<String, String> errors) {
        req.setAttribute("formType", formType);
        req.setAttribute("formFields", form.getFields());
        if (!errors.isEmpty()) {
            req.setAttribute("validationErrors", errors);
            req.setAttribute("formError", "Виявлено помилки валідації. Виправте виділені поля форми.");
        }
    }

    private void redirectWithFlash(HttpServletRequest req, HttpServletResponse resp, String route, String message)
            throws IOException {
        req.getSession().setAttribute(FLASH, message);
        resp.sendRedirect(req.getContextPath() + "/admin/" + route);
    }

    private static String fileNote(FileItem<?> item, String savedName) {
        return item == null || savedName == null ? ""
                : ". Файл «" + item.getName() + "» (" + item.getSize() + " байт) збережено як " + savedName;
    }

    private static String friendly(Exception ex) {
        return ex instanceof SQLException ? "помилка бази даних" : ex.getMessage();
    }

    private void notFound(HttpServletRequest req, HttpServletResponse resp, String route) throws ServletException, IOException {
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        req.setAttribute("invalidRoute", "/admin/" + route);
        render(req, resp, "404");
    }

    /** Відображення сторінки адмінки у загальному шаблоні (index.jsp -> admin.jsp -> admin_{page}.jsp) */
    private void render(HttpServletRequest req, HttpServletResponse resp, String page) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute(FLASH) != null) {
            req.setAttribute("formMessage", session.getAttribute(FLASH));
            session.removeAttribute(FLASH);
        }
        if (!"404".equals(page)) {
            try {
                dataContext.ensureInstalled();
                req.setAttribute("sections", dataContext.sections().getAll());
                req.setAttribute("sectionTree", dataContext.sections().getTree());
                req.setAttribute("products", dataContext.products().getAll());
                if ("home".equals(page)) {
                    req.setAttribute("productsCount", dataContext.products().count());
                }
            } catch (SQLException ex) {
                LOGGER.log(System.Logger.Level.ERROR, "admin render", ex);
                req.setAttribute("dbError", "База даних недоступна: дані каталогу не завантажено");
            }
        }
        req.setAttribute("servlet", "Admin");
        req.setAttribute("adminPage", page);
        req.setAttribute("adminView", "/admin_" + page + ".jsp");
        Views.layout(req, resp);
    }
}

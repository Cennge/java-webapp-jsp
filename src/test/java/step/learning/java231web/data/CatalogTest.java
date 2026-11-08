package step.learning.java231web.data;

import java.util.List;
import org.junit.jupiter.api.Test;
import step.learning.java231web.data.dao.ProductSectionDao;
import step.learning.java231web.data.dto.ProductSection;
import step.learning.java231web.rest.RestPagination;
import step.learning.java231web.rest.RestServlet;
import step.learning.java231web.rest.RestStatus;
import step.learning.java231web.util.Html;

import static org.junit.jupiter.api.Assertions.*;

public class CatalogTest {

    private static ProductSection s(String id, String parentId, String title) {
        return new ProductSection().setId(id).setParentId(parentId).setTitle(title);
    }

    @Test
    public void testBuildTreeNestsChildrenAtAnyDepth() {
        List<ProductSection> roots = ProductSectionDao.buildTree(List.of(
                s("kb", "gaming", "Клавіатури"),
                s("gaming", null, "Ігрові"),
                s("ajazz", "kb", "Ajazz"),
                s("tv", null, "Телевізори"),
                s("orphan", "missing", "Без батька")));

        assertEquals(List.of("gaming", "tv", "orphan"), roots.stream().map(ProductSection::getId).toList());
        ProductSection kb = roots.get(0).getChildren().get(0);
        assertEquals("kb", kb.getId());
        assertEquals("ajazz", kb.getChildren().get(0).getId());
        assertTrue(roots.get(1).getChildren().isEmpty());
    }

    @Test
    public void testPagination() {
        RestPagination p = new RestPagination(3, 10, 25);
        assertEquals(3, p.getTotalPages());
        assertEquals(20, p.getOffset());

        assertEquals(1, new RestPagination(1, 10, 0).getTotalPages(), "порожня колекція - одна сторінка");
        assertEquals(1, new RestPagination(0, -5, 3).getPage());
        assertEquals(RestPagination.DEFAULT_PAGE_SIZE, new RestPagination(0, -5, 3).getPageSize());
    }

    @Test
    public void testHttpCodeOfNonStandardStatuses() {
        assertEquals(200, RestServlet.httpCodeOf(RestStatus.Ok));
        assertEquals(404, RestServlet.httpCodeOf(RestStatus.NotFound));
        assertEquals(401, RestServlet.httpCodeOf(RestStatus.HeaderRequired));
        assertEquals(401, RestServlet.httpCodeOf(RestStatus.TokenExpired));
        assertEquals(400, RestServlet.httpCodeOf(RestStatus.QueryParameterMalformed), "помилка параметра - не 401");
        assertEquals(400, RestServlet.httpCodeOf(RestStatus.ValidationFailed));
        assertEquals(500, RestServlet.httpCodeOf(RestStatus.DatabaseError));
    }

    @Test
    public void testHtmlEscape() {
        assertEquals("&lt;script&gt;alert(&quot;x&quot;)&lt;/script&gt;", Html.esc("<script>alert(\"x\")</script>"));
        assertEquals("Tom &amp; Jerry&#39;s", Html.esc("Tom & Jerry's"));
        assertEquals("", Html.esc(null));
    }
}

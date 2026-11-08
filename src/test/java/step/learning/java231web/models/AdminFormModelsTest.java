package step.learning.java231web.models;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import step.learning.java231web.TestFiles;

import static org.junit.jupiter.api.Assertions.*;

public class AdminFormModelsTest {

    private static final byte[] PNG = { (byte) 0x89, 'P', 'N', 'G' };

    private static Map<String, String> validSection() {
        Map<String, String> f = new HashMap<>();
        f.put("section-title", "Побутова техніка");
        f.put("section-description", "Техніка для дому");
        f.put("section-slug", "home-appliances");
        f.put("section-order", "10");
        return f;
    }

    private static Map<String, String> validProduct() {
        Map<String, String> f = new HashMap<>();
        f.put("product-section-id", "3bf2933c-2172-4b5e-a464-89d1bfc872a5");
        f.put("product-title", "Samsung Galaxy S30");
        f.put("product-description", "Флагманський смартфон");
        f.put("product-slug", "samsung-galaxy-s30");
        f.put("product-price", "15999,50");
        f.put("product-stock", "7");
        f.put("product-order", "1");
        return f;
    }

    @Test
    public void testValidSectionWithImage() {
        SectionFormModel model = SectionFormModel.fromForm(
                TestFiles.form(validSection(), Map.of("section-image", TestFiles.file("tv.png", PNG))));
        assertTrue(model.isValid(), model.getValidationErrors().toString());
        assertNull(model.getParentId(), "порожній батько -> кореневий розділ");
    }

    @Test
    public void testSectionErrorsPerField() {
        Map<String, String> f = validSection();
        f.put("section-title", "A");
        f.put("section-slug", "Home Appliances");
        f.put("section-order", "-1");
        Map<String, String> errors = SectionFormModel.fromForm(TestFiles.form(f, Map.of())).getValidationErrors();

        assertEquals("Поле «Назва розділу» повинно містити щонайменше 2 символи", errors.get("section-title"));
        assertTrue(errors.get("section-slug").startsWith("Слаг може містити лише"));
        assertTrue(errors.get("section-order").contains("від'ємним"));
        assertTrue(errors.containsKey("section-image"), "зображення розділу обов'язкове");
        assertFalse(errors.containsKey("section-description"));
    }

    @Test
    public void testImageExtensionRules() {
        Map<String, String> errors = SectionFormModel.fromForm(TestFiles.form(validSection(),
                Map.of("section-image", TestFiles.file("virus.exe", PNG)))).getValidationErrors();
        assertTrue(errors.get("section-image").startsWith("Непідтримуваний формат"));

        errors = SectionFormModel.fromForm(TestFiles.form(validSection(),
                Map.of("section-image", TestFiles.file("noext", PNG)))).getValidationErrors();
        assertTrue(errors.get("section-image").contains("розширення"));
    }

    @Test
    public void testValidProductAcceptsCommaPrice() {
        ProductFormModel model = ProductFormModel.fromForm(
                TestFiles.form(validProduct(), Map.of("product-image", TestFiles.file("s30.webp", PNG))));
        assertTrue(model.isValid(), model.getValidationErrors().toString());
        assertEquals(0, new java.math.BigDecimal("15999.50").compareTo(FormValidation.parsePrice(model.getPrice())));
    }

    @Test
    public void testProductPriceRules() {
        for (String bad : new String[] { "abc", "0", "-5", "10.999" }) {
            Map<String, String> f = validProduct();
            f.put("product-price", bad);
            Map<String, String> errors = ProductFormModel.fromForm(
                    TestFiles.form(f, Map.of("product-image", TestFiles.file("s30.webp", PNG)))).getValidationErrors();
            assertTrue(errors.containsKey("product-price"), "ціна '" + bad + "' має бути відхилена");
        }
    }

    @Test
    public void testProductRequiresSectionAndImage() {
        Map<String, String> f = validProduct();
        f.remove("product-section-id");
        Map<String, String> errors = ProductFormModel.fromForm(TestFiles.form(f, Map.of())).getValidationErrors();
        assertEquals("Оберіть розділ товару", errors.get("product-section-id"));
        assertTrue(errors.containsKey("product-image"));
    }

    @Test
    public void testVersionImageIsOptional() {
        Map<String, String> f = new HashMap<>();
        f.put("product-id", "29cc99c6-ab15-4a6a-8e19-12b91244401a");
        f.put("version-title", "Cosmic Red");
        f.put("version-description", "Колір: червоний");
        f.put("version-slug", "cosmic-red");
        f.put("version-price", "16999");
        f.put("version-stock", "3");
        f.put("version-order", "2");
        assertTrue(ProductVersionFormModel.fromForm(TestFiles.form(f, Map.of())).isValid());

        f.put("version-slug", "Cosmic_Red");
        assertTrue(ProductVersionFormModel.fromForm(TestFiles.form(f, Map.of()))
                .getValidationErrors().containsKey("version-slug"));
    }

    @Test
    public void testSymbolsPlural() {
        assertEquals("1 символ", FormValidation.symbols(1));
        assertEquals("2 символи", FormValidation.symbols(2));
        assertEquals("5 символів", FormValidation.symbols(5));
        assertEquals("12 символів", FormValidation.symbols(12));
        assertEquals("21 символ", FormValidation.symbols(21));
        assertEquals("128 символів", FormValidation.symbols(128));
    }
}

package step.learning.java231web.rest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RestStatusTest {

    @Test
    public void testStandardSuccessStatuses() {
        assertTrue(RestStatus.Ok.isOk());
        assertEquals(200, RestStatus.Ok.getCode());
        assertEquals("OK", RestStatus.Ok.getMessage());

        assertTrue(RestStatus.Created.isOk());
        assertEquals(201, RestStatus.Created.getCode());
        assertEquals("Created", RestStatus.Created.getMessage());

        assertTrue(RestStatus.Accepted.isOk());
        assertEquals(202, RestStatus.Accepted.getCode());

        assertTrue(RestStatus.NoContent.isOk());
        assertEquals(204, RestStatus.NoContent.getCode());
    }

    @Test
    public void testStandardClientErrorStatuses() {
        assertFalse(RestStatus.BadRequest.isOk());
        assertEquals(400, RestStatus.BadRequest.getCode());
        assertEquals("Bad Request", RestStatus.BadRequest.getMessage());

        assertFalse(RestStatus.Unauthorized.isOk());
        assertEquals(401, RestStatus.Unauthorized.getCode());

        assertFalse(RestStatus.Forbidden.isOk());
        assertEquals(403, RestStatus.Forbidden.getCode());

        assertFalse(RestStatus.NotFound.isOk());
        assertEquals(404, RestStatus.NotFound.getCode());

        assertFalse(RestStatus.MethodNotAllowed.isOk());
        assertEquals(405, RestStatus.MethodNotAllowed.getCode());

        assertFalse(RestStatus.Conflict.isOk());
        assertEquals(409, RestStatus.Conflict.getCode());

        assertFalse(RestStatus.UnprocessableEntity.isOk());
        assertEquals(422, RestStatus.UnprocessableEntity.getCode());

        assertFalse(RestStatus.TooManyRequests.isOk());
        assertEquals(429, RestStatus.TooManyRequests.getCode());
    }

    @Test
    public void testStandardServerErrorStatuses() {
        assertFalse(RestStatus.InternalServerError.isOk());
        assertEquals(500, RestStatus.InternalServerError.getCode());

        assertFalse(RestStatus.NotImplemented.isOk());
        assertEquals(501, RestStatus.NotImplemented.getCode());

        assertFalse(RestStatus.BadGateway.isOk());
        assertEquals(502, RestStatus.BadGateway.getCode());

        assertFalse(RestStatus.ServiceUnavailable.isOk());
        assertEquals(503, RestStatus.ServiceUnavailable.getCode());
    }

    @Test
    public void testNonStandardStatusesFromLecture() {
        assertFalse(RestStatus.HeaderRequired.isOk());
        assertEquals(440, RestStatus.HeaderRequired.getCode());
        assertEquals("Header Required", RestStatus.HeaderRequired.getMessage());

        assertFalse(RestStatus.HeaderMalformed.isOk());
        assertEquals(441, RestStatus.HeaderMalformed.getCode());
        assertEquals("Header Malformed", RestStatus.HeaderMalformed.getMessage());

        assertFalse(RestStatus.QueryParameterRequired.isOk());
        assertEquals(442, RestStatus.QueryParameterRequired.getCode());
        assertEquals("Query Parameter Required", RestStatus.QueryParameterRequired.getMessage());
    }

    @Test
    public void testProposedNonStandardStatusesForHomework() {
        assertFalse(RestStatus.QueryParameterMalformed.isOk());
        assertEquals(443, RestStatus.QueryParameterMalformed.getCode());

        assertFalse(RestStatus.TokenRequired.isOk());
        assertEquals(444, RestStatus.TokenRequired.getCode());

        assertFalse(RestStatus.TokenExpired.isOk());
        assertEquals(445, RestStatus.TokenExpired.getCode());

        assertFalse(RestStatus.TokenMalformed.isOk());
        assertEquals(446, RestStatus.TokenMalformed.getCode());

        assertFalse(RestStatus.ValidationFailed.isOk());
        assertEquals(447, RestStatus.ValidationFailed.getCode());

        assertFalse(RestStatus.LoginOccupied.isOk());
        assertEquals(448, RestStatus.LoginOccupied.getCode());

        assertFalse(RestStatus.AccountLocked.isOk());
        assertEquals(449, RestStatus.AccountLocked.getCode());

        assertFalse(RestStatus.DatabaseError.isOk());
        assertEquals(550, RestStatus.DatabaseError.getCode());

        assertFalse(RestStatus.ExternalServiceUnavailable.isOk());
        assertEquals(551, RestStatus.ExternalServiceUnavailable.getCode());
    }

    @Test
    public void testRedirectAndExtraStandardStatuses() {
        assertTrue(RestStatus.MovedPermanently.isOk());
        assertEquals(301, RestStatus.MovedPermanently.getCode());
        assertEquals(304, RestStatus.NotModified.getCode());
        assertEquals(206, RestStatus.PartialContent.getCode());
        assertFalse(RestStatus.Locked.isOk());
        assertEquals(423, RestStatus.Locked.getCode());
        assertEquals(451, RestStatus.UnavailableForLegalReasons.getCode());
        assertEquals(552, RestStatus.ConfigurationError.getCode());
    }

    @Test
    public void testFromCodeAndValues() {
        assertSame(RestStatus.NotFound, RestStatus.fromCode(404));
        assertSame(RestStatus.ValidationFailed, RestStatus.fromCode(447));
        assertNull(RestStatus.fromCode(999));

        var all = RestStatus.values();
        assertEquals(RestStatus.Ok, all.get(0));
        assertTrue(all.contains(RestStatus.LoginOccupied));
        // коди унікальні
        assertEquals(all.size(), all.stream().mapToInt(RestStatus::getCode).distinct().count());
        // isOk узгоджений з кодом: < 400 - успіх
        for (RestStatus s : all) {
            assertEquals(s.getCode() < 400, s.isOk(), s.toString());
        }
        assertThrows(UnsupportedOperationException.class, () -> all.add(RestStatus.Ok));
    }

    @Test
    public void testStandardVsNonStandard() {
        assertTrue(RestStatus.Ok.isStandard());
        assertTrue(RestStatus.TooManyRequests.isStandard());
        assertTrue(RestStatus.UnavailableForLegalReasons.isStandard());
        assertFalse(RestStatus.HeaderRequired.isStandard());
        assertFalse(RestStatus.AccountLocked.isStandard());
        assertFalse(RestStatus.DatabaseError.isStandard());
    }

    @Test
    public void testPredefinedStatusesAreImmutable() {
        assertThrows(UnsupportedOperationException.class, () -> RestStatus.Ok.setMessage("hacked"));
        assertThrows(UnsupportedOperationException.class, () -> RestStatus.Ok.setCode(500));
        assertThrows(UnsupportedOperationException.class, () -> RestStatus.Ok.setOk(false));
        assertEquals("OK", RestStatus.Ok.getMessage());

        RestStatus custom = RestStatus.NotFound.withMessage("User not found");
        assertEquals(404, custom.getCode());
        assertFalse(custom.isOk());
        assertEquals("User not found", custom.getMessage());
        assertEquals("Not Found", RestStatus.NotFound.getMessage());
        assertDoesNotThrow(() -> custom.setMessage("changed"));
    }

    @Test
    public void testSerializationHidesInternalFlag() {
        String json = new com.google.gson.Gson().toJson(RestStatus.Ok);
        assertEquals("{\"isOk\":true,\"code\":200,\"message\":\"OK\"}", json);
    }

    @Test
    public void testRestMetaDataTypeAndLinks() {
        assertEquals("null", RestMeta.typeOf(null));
        assertEquals("string", RestMeta.typeOf("Invalid login or password"));
        assertEquals("number", RestMeta.typeOf(42));
        assertEquals("boolean", RestMeta.typeOf(true));
        assertEquals("array", RestMeta.typeOf(java.util.List.of(1, 2)));
        assertEquals("array", RestMeta.typeOf(new int[0]));
        assertEquals("object", RestMeta.typeOf(java.util.Map.of("token", "x")));

        RestMeta meta = new RestMeta().addLink("me", "/user/me");
        assertEquals("/user/me", meta.getLinks().get("me"));
        assertEquals(0, meta.getCacheTime());
    }

    @Test
    public void testCustomStatusCreationAndChaining() {
        RestStatus custom = new RestStatus()
                .setOk(true)
                .setCode(200)
                .setMessage("Custom OK");

        assertTrue(custom.isOk());
        assertEquals(200, custom.getCode());
        assertEquals("Custom OK", custom.getMessage());

        RestStatus another = new RestStatus(true, 200, "Custom OK");
        assertEquals(custom, another);
        assertEquals(custom.hashCode(), another.hashCode());
        assertTrue(custom.toString().contains("Custom OK"));
    }
}

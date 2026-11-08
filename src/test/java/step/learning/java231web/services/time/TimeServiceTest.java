package step.learning.java231web.services.time;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import step.learning.java231web.ioc.IocServicesModule;

import static org.junit.jupiter.api.Assertions.*;

public class TimeServiceTest {

    private ITimeService timeService;

    @BeforeEach
    public void setUp() {
        timeService = new SystemTimeService();
    }

    @Test
    public void testTimestamp10FormatAndPrecision() {
        long ts10 = timeService.getTimestamp10();
        String ts10Str = String.valueOf(ts10);

        assertEquals(10, ts10Str.length());
        assertTrue(ts10 > 1700000000L);
    }

    @Test
    public void testTimestamp13FormatAndPrecision() {
        long ts13 = timeService.getTimestamp13();
        String ts13Str = String.valueOf(ts13);

        assertEquals(13, ts13Str.length());
        assertTrue(ts13 > 1700000000000L);
    }

    @Test
    public void testRelationBetweenSecondsAndMillis() {
        long ts10 = timeService.getTimestamp10();
        long ts13 = timeService.getTimestamp13();

        long secondsFromMillis = ts13 / 1000L;
        assertTrue(Math.abs(ts10 - secondsFromMillis) <= 1);
    }

    @Test
    public void testStringMethods() {
        String s10 = timeService.getTimestamp10AsString();
        String s13 = timeService.getTimestamp13AsString();

        assertNotNull(s10);
        assertNotNull(s13);
        assertEquals(10, s10.length());
        assertEquals(13, s13.length());
    }

    @Test
    public void testGuiceDependencyInjection() {
        Injector injector = Guice.createInjector(new IocServicesModule());

        ITimeService injectedTimeService = injector.getInstance(ITimeService.class);
        assertNotNull(injectedTimeService);

        ITimestampService injectedTimestampService = injector.getInstance(ITimestampService.class);
        assertNotNull(injectedTimestampService);

        assertEquals(10, String.valueOf(injectedTimeService.getTimestamp10()).length());
        assertEquals(13, String.valueOf(injectedTimeService.getTimestamp13()).length());
    }
}

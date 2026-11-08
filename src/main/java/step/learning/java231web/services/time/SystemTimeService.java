package step.learning.java231web.services.time;

public class SystemTimeService implements ITimeService, ITimestampService {

    @Override
    public long getTimestamp10() {
        return System.currentTimeMillis() / 1000L;
    }

    @Override
    public long getTimestamp13() {
        return System.currentTimeMillis();
    }

    @Override
    public long getTimestampSeconds() {
        return getTimestamp10();
    }

    @Override
    public long getTimestampMillis() {
        return getTimestamp13();
    }

    @Override
    public String getTimestamp10AsString() {
        return String.valueOf(getTimestamp10());
    }

    @Override
    public String getTimestamp13AsString() {
        return String.valueOf(getTimestamp13());
    }
}

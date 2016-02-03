package tempest.commands;

public class ResponseData {
    public final long queryLatency;

    public ResponseData(long queryLatency) {
        this.queryLatency = queryLatency;
    }

    public long getQueryLatency() {
        return queryLatency;
    }

    public ResponseData add(ResponseData response) {
        return new ResponseData(Math.max(getQueryLatency(), response.getQueryLatency())); //Max rather than the sum for parallel
    }
}

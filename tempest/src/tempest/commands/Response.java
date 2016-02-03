package tempest.commands;

public class Response<TResponse> {
    private TResponse response;
    private ResponseData responseData;

    public TResponse getResponse() {
        return response;
    }

    public void setResponse(TResponse response) {
        this.response = response;
    }

    public ResponseData getResponseData() {
        return responseData;
    }

    public void setResponseData(ResponseData responseData) {
        this.responseData = responseData;
    }
}

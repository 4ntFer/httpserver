import java.util.Map;

public class HttpRequest {
    private final String method;
    private final String url;
    private final Map<String, String> headers;

    public HttpRequest(String method, String url, Map<String, String> headers){
        this.method = method;
        this.url = url;
        this.headers = headers;
    }

    public String toString(){
        return "method: " + method + "\n" +
                "url: " + url + "\n";
    }

    public String getUrl() {
        return url;
    }

    public String getMethod(){
        return method;
    }
}

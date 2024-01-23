package currencyexchanger;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HtmlRequester {
    private static final Logger logger = Logger.getLogger(HtmlRequester.class.getName());

    private final URL url;
    private final Map<String, String> headers;

    public HtmlRequester(String url) throws MalformedURLException {
        this(url, null);
    }
    public HtmlRequester(String url, Map<String, String> headers) throws MalformedURLException {
        this.url = new URL(url);
        this.headers = headers;
    }

    private String stringifyParameters(Map<String, String> parameters) {
        return String.join("&", parameters.entrySet().stream().map(entry -> String.format("%s=%s",
                URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8),
                URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)
        )).toList());
    }

    private HttpURLConnection getConnection() throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);
        con.setDoOutput(true);
        if (headers != null) {
            headers.forEach(con::setRequestProperty);
        }
        return con;
    }

    public String sendRequest(RequestType type) throws IOException {
        return sendRequest(type, null);
    }

    public String sendRequest(RequestType type, Map<String, String> parameters) throws IOException {
        HttpURLConnection con = getConnection();
        con.setRequestMethod(type.string());
        if (parameters != null) {
            try (DataOutputStream dos = new DataOutputStream(con.getOutputStream())) {
                dos.writeBytes(stringifyParameters(parameters));
            }
        }

        //Request is sent here
        con.connect();

        int statusCode = con.getResponseCode();
        String logMessage = String.format("Request returned with status %s %s", statusCode, con.getResponseMessage());
        InputStreamReader responseReader;
        if (statusCode <= 299) {
            logger.log(Level.INFO, logMessage);
            responseReader = new InputStreamReader(con.getInputStream());
        } else {
            logger.log(Level.WARNING, logMessage);
            responseReader = new InputStreamReader(con.getErrorStream());
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(responseReader)) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        }
        return response.toString();
    }

    public enum RequestType {
        GET("GET"),
        POST("POST"),
        HEAD("HEAD"),
        OPTIONS("OPTIONS"),
        PUT("PUT"),
        DELETE("DELETE"),
        TRAC("TRAC");

        private final String string;

        RequestType(String string) {
            this.string = string;
        }

        public String string() {
            return string;
        }
    }
}

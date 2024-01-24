package currencyexchanger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HtmlRequester {
    private static final Logger LOGGER = Logger.getLogger(HtmlRequester.class.getName());

    private final String endpointUrl;
    private final Map<String, String> headers;

    /**
     * Creates a requester object that can send HTML requests to the provided {@code url} endpoint.
     *
     * @param endpointUrl The endpoint URL to send requests to
     * @see #HtmlRequester(String, Map)
     */
    public HtmlRequester(String endpointUrl) {
        this(endpointUrl, null);
    }

    /**
     * Creates a requester object that can send HTML requests to the provided {@code url} endpoint with the specified headers.
     *
     * @param endpointUrl The endpoint URL to send requests to
     * @param headers     Any headers that should be included in requests sent by this requester
     * @see #HtmlRequester(String)
     */
    public HtmlRequester(String endpointUrl, Map<String, String> headers) {
        this.endpointUrl = endpointUrl + "?";
        this.headers = headers;
    }

    private String stringifyParameters(Map<String, String> parameters) {
        return String.join("&", parameters.entrySet().stream().map(entry -> String.format("%s=%s",
                URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8),
                URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)
        )).toList());
    }

    public JsonNode sendRequest(RequestType type) throws IOException {
        return sendRequest(type, null);
    }

    public JsonNode sendRequest(RequestType type, Map<String, String> parameters) throws IOException {
        URL url = new URL(parameters == null || parameters.isEmpty()
                ? endpointUrl
                : endpointUrl + stringifyParameters(parameters)
        );
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(type.string());
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);
        if (headers != null) {
            headers.forEach(con::setRequestProperty);
        }

        //Request is sent here
        con.connect();

        int statusCode = con.getResponseCode();
        String logMessage = String.format("Request returned with status %s %s", statusCode, con.getResponseMessage());
        InputStreamReader responseReader;
        if (statusCode <= 299) {
            LOGGER.log(Level.INFO, logMessage);
            responseReader = new InputStreamReader(con.getInputStream());
        } else {
            LOGGER.log(Level.WARNING, logMessage);
            responseReader = new InputStreamReader(con.getErrorStream());
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(responseReader)) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        }
        con.disconnect();
        return new ObjectMapper().readTree(response.toString());
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

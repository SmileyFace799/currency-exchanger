package currencyexchanger;

import com.fasterxml.jackson.core.JsonProcessingException;
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

/**
 * A generic class to simplify the process of sending HTML requests to an API.
 */
class HtmlRequester {
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

    /**
     * Takes request parameters & turns them into a string that can be added to the URL
     *
     * @param parameters The parameters to stringify
     * @return The stringified parameters
     */
    private String stringifyParameters(Map<String, String> parameters) {
        return String.join("&", parameters.entrySet().stream().map(entry -> String.format("%s=%s",
                URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8),
                URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)
        )).toList());
    }

    /**
     * Sends an HTML request to this endpoint, with no parameters.
     *
     * @param type The type of request to send
     * @return The response from the request
     * @throws IOException If an {@link IOException} occurs when requesting
     * @see #sendRequest(RequestType, Map)
     */
    public Response sendRequest(RequestType type) throws IOException {
        return sendRequest(type, null);
    }

    /**
     * Sends an HTML request to this endpoint.
     *
     * @param type       The type of request to send
     * @param parameters The parameters to include in the request
     * @return The response from the request
     * @throws IOException If an {@link IOException} occurs when requesting
     * @see #sendRequest(RequestType)
     */
    public Response sendRequest(RequestType type, Map<String, String> parameters) throws IOException {
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
        if (statusCode < 300) {
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
        return new Response(statusCode, con.getResponseMessage(), response.toString());
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

    /**
     * A response form an HTML request
     */
    static class Response {
        private final int statusCode;
        private final String statusMessage;
        private final String rawData;
        private final JsonNode json;

        /**
         * Creates the response object
         *
         * @param statusCode    The response status code
         * @param statusMessage The response status message
         * @param rawData       The raw data received in the response
         */
        private Response(int statusCode, String statusMessage, String rawData) {
            this.statusCode = statusCode;
            this.statusMessage = statusMessage;
            this.rawData = rawData;

            JsonNode parsedJson;
            try {
                parsedJson = new ObjectMapper().readTree(rawData);
            } catch (JsonProcessingException jpe) {
                parsedJson = null;
            }
            json = parsedJson;
        }

        /**
         * Returns the response status code.
         *
         * @return The response status code
         *         <ul><li>If {@code code < 300}, the request was successful</li>
         *         <li>If {@code code >= 300}, the request failed</li></ul>
         */
        public int getStatusCode() {
            return statusCode;
        }

        /**
         * Returns the response status message.
         *
         * @return The response status message
         */
        public String getStatusMessage() {
            return statusMessage;
        }

        /**
         * Returns the raw response data.
         *
         * @return The raw response data
         */
        public String getRawData() {
            return rawData;
        }

        /**
         * Returns whether the data can be interpreted as json or not.
         *
         * @return <ul><li><b>{@code true}</b> - The data is json</li>
         *         <li><b>{@code false}</b> - The data cannot be interpreted as json</li></ul>
         */
        public boolean isJson() {
            return json != null;
        }

        /**
         * Returns the response data as a json tree.
         *
         * @return The response data as json.
         *         If the response data is not json, this will be {@code null}.
         * @see #isJson()
         */
        public JsonNode getJson() {
            return json;
        }
    }
}

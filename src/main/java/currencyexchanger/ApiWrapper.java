package currencyexchanger;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Wrapper class for making HTTP requests to <a href="https://freecurrencyapi.com/">https://freecurrencyapi.com/</a>
 */
class ApiWrapper {
    private static final String BASE_API_URL = "https://api.freecurrencyapi.com/";
    private static final String LATEST_ENDPOINT = "v1/latest";

    private final HtmlRequester latestRatesRequester;

    /**
     * Creates the wrapper
     *
     * @param apiKey The API key to use when making requests
     */
    public ApiWrapper(String apiKey) {
        latestRatesRequester = new HtmlRequester(BASE_API_URL + LATEST_ENDPOINT, Map.of("apikey", apiKey));
    }

    /**
     * Makes a request for all available latest exchange rates, with USD as the base currency.
     *
     * @return A map containing the exchange rates for USD to all available currencies
     * @throws RequestFailedException If the API request fails
     * @throws IOException If an {@link IOException} occurs when requesting
     * @see #requestLatestRates(String)
     * @see #requestLatestRates(Collection)
     * @see #requestLatestRates(String, Collection)
     * @see <a href=https://freecurrencyapi.com/docs/currency-list>All available currencies</a>
     */
    public Map<String, Double> requestLatestRates() throws RequestFailedException, IOException {
        return requestLatestRates(null, null);
    }

    /**
     * Makes a request for all available latest exchange rates.
     *
     * @param baseCurrency The base currency to get exchange rates for
     * @return A map containing the exchange rates for {@code baseCurrency} to all available currencies
     * @throws RequestFailedException If the API request fails
     * @throws IOException If an {@link IOException} occurs when requesting
     * @see #requestLatestRates()
     * @see #requestLatestRates(Collection)
     * @see #requestLatestRates(String, Collection)
     * @see <a href=https://freecurrencyapi.com/docs/currency-list>All available currencies</a>
     */
    public Map<String, Double> requestLatestRates(String baseCurrency) throws RequestFailedException, IOException {
        return requestLatestRates(baseCurrency, null);
    }

    /**
     * Makes a request for the latest exchange rates, with USD as the base currency
     *
     * @param currencies All currencies to get the exchange rate to
     * @return A map with {@code value}s containing the exchange rates for USD to {@code key}
     * @throws RequestFailedException If the API request fails
     * @throws IOException If an {@link IOException} occurs when requesting
     * @see #requestLatestRates()
     * @see #requestLatestRates(String)
     * @see #requestLatestRates(String, Collection)
     * @see <a href=https://freecurrencyapi.com/docs/currency-list>All available currencies</a>
     */
    public Map<String, Double> requestLatestRates(Collection<String> currencies) throws RequestFailedException, IOException {
        return requestLatestRates(null, currencies);
    }

    /**
     * Makes a request for the latest exchange rates.
     *
     * @param baseCurrency The base currency to get exchange rates for
     * @param currencies All currencies to get the exchange rate to
     * @return A map with {@code value}s containing the exchange rates for {@code baseCurrency} to {@code key}
     * @throws RequestFailedException If the API request fails
     * @throws IOException If an {@link IOException} occurs when requesting
     * @see #requestLatestRates()
     * @see #requestLatestRates(String)
     * @see #requestLatestRates(Collection)
     * @see <a href=https://freecurrencyapi.com/docs/currency-list>All available currencies</a>
     */
    public Map<String, Double> requestLatestRates(
            String baseCurrency,
            Collection<String> currencies
    ) throws RequestFailedException, IOException {
        Map<String, String> parameters = new HashMap<>();
        if (baseCurrency != null) {
            parameters.put("base_currency", baseCurrency);
        }
        if (currencies != null && !currencies.isEmpty()) {
            parameters.put("currencies", String.join(",", currencies));
        }
        HtmlRequester.Response response = latestRatesRequester.sendRequest(HtmlRequester.RequestType.GET, parameters);

        Map<String, Double> exchangeRates = new HashMap<>();
        if (response.getStatusCode() < 300 && response.isJson()) {
            JsonNode json = response.getJson();
            if (json.has("data")) {
                json = json.get("data");
            }
            for (Iterator<String> it = json.fieldNames(); it.hasNext(); ) {
                String currencyName = it.next();
                exchangeRates.put(currencyName, json.get(currencyName).asDouble());
            }
        } else {
            throw new RequestFailedException(
                    response.getStatusCode(),
                    response.getStatusMessage(),
                    response.getRawData()
            );
        }
        return exchangeRates;
    }

    /**
     * Thrown when an API request fails.
     */
    static class RequestFailedException extends Exception {
        /**
         * Creates a {@link RequestFailedException}.
         *
         * @param statusCode The status code in the response of the failed request
         * @param statusMessage The status message in the response of the failed request
         * @param rawData The data returned by the failed request
         */
        public RequestFailedException(int statusCode, String statusMessage, String rawData) {
            super(String.format("Response status: %s %s | Response data: %s", statusCode, statusMessage, rawData));
        }
    }
}

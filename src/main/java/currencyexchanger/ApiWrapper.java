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
public class ApiWrapper {
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

    public Map<String, Double> requestLatestRates() throws IOException {
        return requestLatestRates(null, null);
    }

    public Map<String, Double> requestLatestRates(String baseCurrency) throws IOException {
        return requestLatestRates(baseCurrency, null);
    }

    public Map<String, Double> requestLatestRates(Collection<String> currencies) throws IOException {
        return requestLatestRates(null, currencies);
    }

    public Map<String, Double> requestLatestRates(String baseCurrency, Collection<String> currencies) throws IOException {
        Map<String, String> parameters = new HashMap<>();
        if (baseCurrency != null) {
            parameters.put("base_currency", baseCurrency);
        }
        if (currencies != null && !currencies.isEmpty()) {
            parameters.put("currencies", String.join(",", currencies));
        }
        JsonNode response = latestRatesRequester.sendRequest(HtmlRequester.RequestType.GET, parameters);

        Map<String, Double> exchangeRates = new HashMap<>();
        if (response.has("data")) {
            response = response.get("data");
        }
        for (Iterator<String> it = response.fieldNames(); it.hasNext();) {
            String currencyName = it.next();
            exchangeRates.put(currencyName, response.get(currencyName).asDouble());
        }
        return exchangeRates;
    }
}

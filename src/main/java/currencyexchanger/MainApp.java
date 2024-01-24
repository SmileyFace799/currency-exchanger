package currencyexchanger;

import java.io.IOException;
import java.util.Set;

public class MainApp {
    public static void main(String[] args) throws IOException {
        ApiWrapper api = new ApiWrapper("REDACTED");
        System.out.println(api.requestLatestRates("NOK", Set.of("SEK", "DKK", "USD", "EUR")));
    }
}

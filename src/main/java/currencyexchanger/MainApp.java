package currencyexchanger;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Example of how to use the {@link RatesManager}.
 */
class MainApp {
    /**
     * Starts the example code.
     *
     * @param args Should contain an API key for <a href="https://freecurrencyapi.com/">
     *             https://freecurrencyapi.com/</a> as the 1st argument
     */
    public static void main(String[] args) {
        if (args.length < 1 || args[0] == null) {
            throw new IllegalArgumentException("An API key should be provided as the 1st argument");
        }

        RatesManager ratesManager = new RatesManager.Builder(args[0])
                .setBaseCurrency("NOK")
                .setCurrencies("SEK", "DKK", "USD", "EUR")
                .build();

        ratesManager.addListener(rates -> Logger.getLogger(MainApp.class.getName()).log(Level.INFO, "Received rates: {0}", rates));
        ratesManager.start(30, TimeUnit.MINUTES);
    }
}

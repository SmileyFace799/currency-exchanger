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
        // Guard condition to verify that an API key was provided
        if (args.length < 1 || args[0] == null) {
            throw new IllegalArgumentException("An API key should be provided as the 1st argument");
        }

        // Creates the RatesManager
        RatesManager ratesManager = new RatesManager.Builder(args[0])
                .setBaseCurrency("NOK") // Sets the base currency
                .setCurrencies("SEK", "DKK", "USD", "EUR") // Sets the currencies to retrieve exchange rates to
                .build();

        // Creates a simple listener that logs any received rate updates, so they appear in the console
        RatesUpdateListener logReceivedRates = rates -> Logger.getLogger(MainApp.class.getName()).log(Level.INFO, "Received rates: {0}", rates);
        ratesManager.addListener(logReceivedRates); // Adds the listener
        ratesManager.start(30, TimeUnit.MINUTES); // Starts the manager, & sets it to retrieve updated rates every 30min
    }
}

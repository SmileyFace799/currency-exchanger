package currencyexchanger;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manager class that periodically retrieves the latest exchange rates,
 * using the <a href="https://freecurrencyapi.com/">https://freecurrencyapi.com/</a> API.
 *
 * @see Builder
 */
public class RatesManager {
    private static final Logger LOGGER = Logger.getLogger(RatesManager.class.getName());

    private final ScheduledExecutorService scheduler;
    private final Collection<RatesUpdateListener> listeners;

    private final ApiWrapper api;
    private final String baseCurrency;
    private final Collection<String> currencies;

    private ScheduledFuture<?> updateTask;

    /**
     * Creates the manager.
     *
     * @param builder The configured builder to use when constructing the manager.
     */
    private RatesManager(Builder builder) {
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.listeners = new HashSet<>();

        this.api = new ApiWrapper(builder.apiKey);
        this.baseCurrency = builder.baseCurrency == null || builder.baseCurrency.isBlank() ? null : builder.baseCurrency;
        this.currencies = builder.currencies == null || builder.currencies.isEmpty() ? null : builder.currencies;

        this.updateTask = null;
    }

    /**
     * Adds a listener to listen for updated exchange rates.
     *
     * @param listener the listener to add
     */
    public void addListener(RatesUpdateListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Called periodically to retrieve updated rates and notify any listeners.
     */
    private void updateRates() {
        try {
            Map<String, Double> rates = api.requestLatestRates(baseCurrency, currencies);
            listeners.forEach(listener -> listener.onUpdate(rates));
        } catch (ApiWrapper.RequestFailedException rfe) {
            LOGGER.log(Level.WARNING, "A request for updating rates failed", rfe);
        } catch (IOException ioe) {
            LOGGER.log(Level.WARNING, "Updating rates failed due to an IOException", ioe);
        }
    }

    /**
     * Start retrieving exchange rate updates.
     *
     * @param intervalTime The amount of time between each update
     * @param timeUnit     The unit of time that {@code intervalTime} is specified in
     * @throws IllegalStateException If this {@link RatesManager} is already running
     */
    public void start(long intervalTime, TimeUnit timeUnit) {
        if (updateTask != null) {
            throw new IllegalStateException("This RatesManager is already running");
        }
        this.updateTask = scheduler.scheduleAtFixedRate(this::updateRates, 0, intervalTime, timeUnit);
    }

    /**
     * Stop retrieving exchange rate updates. If an update is happening as this is called,
     * the update will finish, but no further updates will happen.
     *
     * @throws IllegalStateException If this {@link RatesManager} is already stopped
     */
    public void stop() {
        if (updateTask == null) {
            throw new IllegalStateException("This RatesManager is already stopped");
        }
        updateTask.cancel(false);
        this.updateTask = null;
    }

    /**
     * Builder for {@link RatesManager}.
     */
    public static class Builder {
        private final String apiKey;

        private String baseCurrency;
        private Collection<String> currencies;

        /**
         * Creates the builder.
         *
         * @param apiKey An API key for accessing the <a href="https://freecurrencyapi.com/">
         *               https://freecurrencyapi.com/</a> API
         */
        public Builder(String apiKey) {
            this.apiKey = apiKey;

            this.baseCurrency = null;
            this.currencies = null;
        }

        /**
         * Builds the {@link RatesManager}.
         *
         * @return The newly build {@link RatesManager}
         */
        public RatesManager build() {
            return new RatesManager(this);
        }

        /**
         * Sets the base currency to be used when retrieving currency updates.
         * If no base currency is set, USD will be used as the base currency.
         *
         * @param baseCurrency The base currency to use
         * @return The builder
         * @see <a href=https://freecurrencyapi.com/docs/currency-list>All available currencies</a>
         */
        public Builder setBaseCurrency(String baseCurrency) {
            this.baseCurrency = baseCurrency;
            return this;
        }

        /**
         * Sets the currencies to retrieve the exchange rate for. if no currencies are set,
         * exchange rates will be retrieved for all available currencies.
         *
         * @param currencies The currencies to set
         * @return The builder
         * @see <a href=https://freecurrencyapi.com/docs/currency-list>All available currencies</a>
         */
        public Builder setCurrencies(String... currencies) {
            this.currencies = Set.of(currencies);
            return this;
        }
    }
}

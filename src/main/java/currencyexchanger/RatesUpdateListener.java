package currencyexchanger;

import java.util.Map;

/**
 * Listens for updated currency exchange rates.
 */
public interface RatesUpdateListener {
    /**
     * Called whenever the listener receives updated rates.
     *
     * @param rates The new rates received.
     */
    void onUpdate(Map<String, Double> rates);
}

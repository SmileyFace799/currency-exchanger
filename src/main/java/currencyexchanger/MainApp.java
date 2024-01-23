package currencyexchanger;

import java.io.IOException;
import java.util.Map;

public class MainApp {
    public static void main(String[] args) throws IOException {
        HtmlRequester htmlRequester = new HtmlRequester("https://api.freecurrencyapi.com/v1/latest", Map.of("apikey", "REDACTED"));
        System.out.println(htmlRequester.sendRequest(HtmlRequester.RequestType.GET));
    }
}

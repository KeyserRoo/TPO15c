/**
 *
 *  @author Gessek Jan S28970
 *
 */

import java.io.*;
import java.util.*;
import java.net.URI;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

public class Service {
  String country;
  String countryISO;
  String countryCurrency;
  String city;

  String gettingRateFor;

  String apiKeyWeather = "436756183717e5243017d801190091bc";

  // TAK, ukradlem klucz koledze bo nie chcialo
  // mi sie tworzyc konta, a o co chodzi?
  String apiKeyOpenEr = "bfd9d88802fe819b31d71ee5";

  public Service(String cou) {
    country = cou;
    initializeISOAndCurrency();
  }

  public String getCurrency() {
    return countryCurrency;
  }

  public String getISO() {
    return countryISO;
  }

  public String getWeather(String ci) {
    city = ci;
    String url = String.format(
        "http://api.openweathermap.org/data/2.5/weather?q=%s,%s&appid=%s&units=metric", country, ci, apiKeyWeather);
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(new URI(url).toURL().openConnection().getInputStream()))) {

      String json = reader.lines().collect(Collectors.joining());
      return json;

    } catch (Exception e) {
      e.printStackTrace();
    }
    return "";
  }

  public Double getRateFor(String currency) {
    gettingRateFor = currency;
    String url = String.format(
        "https://v6.exchangerate-api.com/v6/%s/latest/%s", apiKeyOpenEr, gettingRateFor);
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(new URI(url).toURL().openConnection().getInputStream()))) {

      String json = reader.lines().collect(Collectors.joining());
      Money money = new Gson().fromJson(json, Money.class);
      return Double.parseDouble(money.conversion_rates.get(countryCurrency));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return -1.0;

  }

  public Double getNBPRate() {
    if (countryISO.equals("PL"))
      return 1.0;

    double toRet = -1;
    toRet = odwiedzNBP("http://api.nbp.pl/api/exchangerates/rates/a/" + countryCurrency + "/?format=json");
    if (toRet == -1)
      toRet = odwiedzNBP("http://api.nbp.pl/api/exchangerates/rates/b/" + countryCurrency + "/?format=json");

    return toRet;
  }

  private double odwiedzNBP(String path) {
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(new URI(path).toURL().openConnection().getInputStream()))) {
      String json = reader.lines().collect(Collectors.joining());
      return JsonParser.parseString(json).getAsJsonObject().getAsJsonArray("rates").get(0).getAsJsonObject().get("mid")
          .getAsDouble();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return -1;
  }

  private void initializeISOAndCurrency() {
    for (Locale locale : Locale.getAvailableLocales()) {
      if (locale.getDisplayCountry(Locale.ENGLISH).equalsIgnoreCase(country)) {
        countryISO = locale.getCountry();
        countryCurrency = Currency.getInstance(locale).getCurrencyCode();
      }
    }
  }

}

class Weather {
  Map<String, String> main = new HashMap<>();
}

class Money {
  Map<String, String> conversion_rates = new HashMap<>();
}
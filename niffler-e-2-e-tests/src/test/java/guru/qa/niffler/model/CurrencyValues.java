package guru.qa.niffler.model;

public enum CurrencyValues {
  RUB("₽"), USD, EUR, KZT;

  public String value;

  CurrencyValues() {
  }

  CurrencyValues(String value) {
    this.value = value;
  }
}

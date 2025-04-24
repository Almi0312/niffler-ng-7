package guru.qa.niffler.api;

import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.model.CurrencyValues;
import guru.qa.niffler.model.SpendJson;
import org.junit.jupiter.api.Assertions;
import retrofit2.Response;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static guru.qa.niffler.api.ApiClient.SPEND_API;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ParametersAreNonnullByDefault
public class SpendApiClient {

  private final SpendApi spendApi = SPEND_API.getINSTANCE().create(SpendApi.class);

  public @Nullable SpendJson createSpend(SpendJson spend) {
    final Response<SpendJson> response;
    try {
      response = spendApi.addSpend(spend).execute();
    } catch (IOException e) {
      throw new AssertionError(e.getMessage());
    }
    assertEquals(201, response.code(), format("Трата [%s] не создана", spend) + response.message());
    return response.body();
  }

  public @Nullable SpendJson getIDSpend(String id) {
    final Response<SpendJson> response;
    try {
      response = spendApi.getIDSpend(id).execute();
    } catch (IOException e) {
      throw new AssertionError(e.getMessage());
    }
    assertEquals(200, response.code(), format("Трата с id [%s] не найдена", id) + response.message());
    return response.body();
  }

  public @Nullable SpendJson editSpend(String id) {
    final Response<SpendJson> response;
    try {
      response = spendApi.getIDSpend(id).execute();
    } catch (IOException e) {
      throw new AssertionError(e.getMessage());
    }
    assertEquals(200, response.code(), format("Трата с id [%s] не найдена", id) + response.message());
    return response.body();
  }

  public @Nullable List<SpendJson> getAllSpends(String username,
                                      @Nullable CurrencyValues currency,
                                      @Nullable String from,
                                      @Nullable String to) {
    final Response<List<SpendJson>> response;
    try {
      response = spendApi.getAllSpend(username, currency, from, to).execute();
    } catch (IOException e) {
      throw new AssertionError(e.getMessage());
    }
    assertEquals(200, response.code(), "Получить траты не удалось: " + response.message());
    return response.body() != null
            ? response.body()
            : Collections.emptyList();
  }

  public void removeSpend(List<String> ids) {
    final Response<Void> response;
    try {
      response = spendApi.removeSpend(ids).execute();
    } catch (IOException e) {
      throw new AssertionError(e.getMessage());
    }
    assertEquals(200, response.code(), "Трата не удалена" + response.message());
  }

  public @Nullable CategoryJson addCategory(CategoryJson category) {
    final Response<CategoryJson> response;
    try {
      response = spendApi.addCategories(category).execute();
    } catch (IOException e) {
      throw new AssertionError();
    }
    assertEquals(200, response.code(), format("Категория %s не была создана", category.toString()));
    return response.body();
  }

  public @Nullable CategoryJson updateCategory(CategoryJson category) {
    final Response<CategoryJson> response;
    try {
      response = spendApi.updateCategories(category).execute();
    } catch (IOException e) {
      throw new AssertionError();
    }
    Assertions.assertEquals(200, response.code(), "Категория не была обновлена");
    return response.body();
  }

  public @Nullable List<CategoryJson> getAllCategories(String username, Boolean excludeArchived) {
    final Response<List<CategoryJson>> response;
    try {
      response = spendApi.allCategories(username, excludeArchived).execute();
    } catch (IOException e) {
      throw new AssertionError();
    }
    Assertions.assertEquals(200, response.code(), "Получить все категории не удалось");
    return response.body() != null
            ? response.body()
            : Collections.emptyList();
  }

}

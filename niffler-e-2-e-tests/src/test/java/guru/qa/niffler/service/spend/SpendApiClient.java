package guru.qa.niffler.service.spend;

import guru.qa.niffler.api.SpendApi;
import guru.qa.niffler.api.core.RestClient;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.model.SpendJson;
import guru.qa.niffler.service.SpendsClient;
import io.qameta.allure.Step;
import org.junit.jupiter.api.Assertions;
import retrofit2.Response;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ParametersAreNonnullByDefault
public class SpendApiClient extends RestClient implements SpendsClient {

    private static final Config CFG = Config.getInstance();

    private final SpendApi spendApi;

    public SpendApiClient() {
        super(CFG.spendUrl());
        this.spendApi = create(SpendApi.class);
    }

    @Override
    @Step("Создать трату(spend) {0} с помощью REST api")
    public @Nonnull SpendJson createSpend(SpendJson spend) {
        final Response<SpendJson> response;
        try {
            response = spendApi.addSpend(spend).execute();
        } catch (IOException e) {
            throw new AssertionError(e.getMessage());
        }
        assertEquals(201, response.code(), response.message());
        return Objects.requireNonNull(response.body());
    }

    @Override
    @Step("Обновить трату(spend) {0} с помощью REST api")
    public @Nonnull SpendJson updateSpend(SpendJson spend) {
        final Response<SpendJson> response;
        try {
            response = spendApi.editSpend(spend).execute();
        } catch (IOException e) {
            throw new AssertionError(e.getMessage());
        }
        assertEquals(200, response.code(), response.message());
        return Objects.requireNonNull(response.body());
    }

    @Override
    @Step("Создать категорию {0} с помощью REST api")
    public @Nonnull CategoryJson createCategory(CategoryJson category) {
        final Response<CategoryJson> response;
        try {
            response = spendApi.addCategory(category).execute();
        } catch (IOException e) {
            throw new AssertionError();
        }
        assertEquals(200, response.code(), response.message());
        CategoryJson newCategory = response.body();
        return newCategory.archived() ?
                updateCategory(new CategoryJson(
                        category.id(),
                        category.name(),
                        category.username(),
                        true))
                : newCategory;
    }

    @Override
    @Step("Найти трату(spend) {0} с помощью REST api")
    public @Nonnull Optional<SpendJson> findById(UUID id) {
        final Response<SpendJson> response;
        try {
            response = spendApi.getIDSpend(id.toString()).execute();
        } catch (IOException e) {
            throw new AssertionError(e.getMessage());
        }
        assertEquals(200, response.code(), response.message());
        return Optional.ofNullable(response.body());
    }

    @Override
    @Step("Найти все траты(spend) пользователя {0} с помощью REST api")
    public @Nonnull List<SpendJson> findAllByUsername(String username) {
        final Response<List<SpendJson>> response;
        try {
            response = spendApi.getAllSpend(username, null, null, null).execute();
        } catch (IOException e) {
            throw new AssertionError(e.getMessage());
        }
        assertEquals(200, response.code(), response.message());
        return response.body() != null
                ? response.body()
                : Collections.emptyList();
    }

    @Override
    @Step("Удалить трату(spend) {0} с помощью REST api")
    public void remove(SpendJson spend) {
        final Response<Void> response;
        try {
            response = spendApi.removeSpend(spend.username(),
                            List.of(spend.id().toString()))
                    .execute();
        } catch (IOException e) {
            throw new AssertionError(e.getMessage());
        }
//        В сваггере 200 код, но в контроллере выдает 202
        assertEquals(202, response.code(), response.message());
    }

    @Override
    @Step("Обновить категорию {0} с помощью REST api")
    public @Nonnull CategoryJson updateCategory(CategoryJson category) {
        final Response<CategoryJson> response;
        try {
            response = spendApi.updateCategories(category).execute();
        } catch (IOException e) {
            throw new AssertionError(e.getMessage());
        }
        Assertions.assertEquals(200, response.code(), response.message());
        return Objects.requireNonNull(response.body());
    }


    @Override
    @Step("Найти трату(spend) {1} пользователя {0} с помощью REST api")
    public @Nonnull Optional<SpendJson> findByUsernameAndDescription(String username, String description) {
        return findAllByUsername(username).stream()
                .filter(spend -> spend.description().equals(description))
                .findFirst();
    }

    @Override
    @Step("Найти категорию {1} пользователя {0} с помощью REST api")
    public @Nonnull Optional<CategoryJson> findCategoryByUsernameAndName(String username, String categoryName) {
        return findAllCategoryByUsername(username).stream()
                .filter(name -> name.name().equals(categoryName))
                .findFirst();
    }

    @Override
    @Step("Найти все категории пользователя {0} с помощью REST api")
    public @Nonnull List<CategoryJson> findAllCategoryByUsername(String username) {
        final Response<List<CategoryJson>> response;
        try {
            response = spendApi.allCategories(username, false).execute();
        } catch (IOException e) {
            throw new AssertionError(e.getMessage());
        }
        Assertions.assertEquals(200, response.code(), response.message());
        return response.body() != null
                ? response.body()
                : Collections.emptyList();
    }


    @Override
    public @Nonnull List<CategoryJson> findAllCategory() {
        throw new UnsupportedOperationException("Метод findAllCategory не реализован в api проекта");
    }

    @Override
    public void removeCategory(CategoryJson category) {
        throw new UnsupportedOperationException("Метод removeCategory не реализован в api проекта");
    }

    @Override
    public @Nonnull Optional<CategoryJson> findCategoryById(UUID id) {
        throw new UnsupportedOperationException("Метод findCategoryById не реализован в api проекта");
    }

    @Override
    public @Nonnull List<SpendJson> findAll() {
        throw new UnsupportedOperationException("Метод findAll не реализован в api проекта");
    }

}

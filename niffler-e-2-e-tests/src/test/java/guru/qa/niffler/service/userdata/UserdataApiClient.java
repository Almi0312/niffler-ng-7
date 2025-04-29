package guru.qa.niffler.service.userdata;

import guru.qa.niffler.api.AuthApi;
import guru.qa.niffler.api.UserdataApi;
import guru.qa.niffler.api.core.ThreadSafeCookieStore;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.model.CurrencyValues;
import guru.qa.niffler.model.TestData;
import guru.qa.niffler.model.rest.UserdataUserJson;
import guru.qa.niffler.service.UsersClient;
import io.qameta.allure.Step;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import retrofit2.Response;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.*;

import static guru.qa.niffler.config.Constants.DEFAULT_PASSWORD;
import static guru.qa.niffler.model.CurrencyValues.RUB;
import static guru.qa.niffler.api.core.RestClient.EmptyRestClient;

@ParametersAreNonnullByDefault
public class UserdataApiClient implements UsersClient {

    private static final Config CFG = Config.getInstance();

    private final UserdataApi userdataApi = new EmptyRestClient(CFG.userdataUrl()).create(UserdataApi.class);
    private final AuthApi authApi = new EmptyRestClient(CFG.authUrl()).create(AuthApi.class);

    @Override
    @Step("Создать пользователя {0} с помощью REST api")
    public @Nonnull UserdataUserJson create(String username, @Nullable CurrencyValues currencyValue, String password) {
        UserdataUserJson userJson;
        try {
            authApi.registerForm().execute();
            authApi.create(username, DEFAULT_PASSWORD, DEFAULT_PASSWORD,
                    ThreadSafeCookieStore.INSTANCE.cookieValue("XSRF-TOKEN")).execute();
            final long start = System.currentTimeMillis();
            final long await = 3000;
            do {
                userJson = userdataApi.getCurrentUserInfo(username).execute().body();
                if (userJson != null && userJson.id() != null) {
                    break;
                }
                Thread.sleep(200);
            } while (System.currentTimeMillis() - start < await);
            if (userJson == null || userJson.id() == null) {
                throw new RuntimeException("Пользователь не создался в течении %s мс".formatted(await));
            }
        } catch (InterruptedException | IOException e) {
            throw new AssertionError(e.getMessage());
        }
        return userJson.addTestData(new TestData(DEFAULT_PASSWORD));
    }

    @Step("Обновить инфо пользователя на {0} с помощью REST api")
    public @Nonnull UserdataUserJson update(UserdataUserJson user) {
        final Response<UserdataUserJson> response;
        try {
            response = userdataApi.updateUser(user).execute();
        } catch (IOException e) {
            throw new AssertionError(e.getMessage());
        }
        Assertions.assertEquals(200, response.code(), response.message());
        return Objects.requireNonNull(response.body());
    }

    @Step("Найти пользователя {0} с помощью REST api")
    public @Nonnull Optional<UserdataUserJson> findByUsername(String username) {
        final Response<UserdataUserJson> response;
        try {
            response = userdataApi.getCurrentUserInfo(username).execute();
        } catch (IOException e) {
            throw new AssertionError(e.getMessage());
        }
        Assertions.assertEquals(200, response.code(), response.message());
        UserdataUserJson userJson = response.body();
        if (userJson.testData() == null) {
            userJson.addTestData(new TestData(DEFAULT_PASSWORD));
        }
        return Optional.of(userJson);
    }

    @Step("Найти всех пользователей с помощью REST api")
    public @Nonnull List<UserdataUserJson> getAllUsers(String username, @Nullable String searchQuery) {
        final Response<List<UserdataUserJson>> response;
        try {
            response = userdataApi.getAllUsersByUsername(username, searchQuery).execute();
        } catch (IOException e) {
            throw new AssertionError(e.getMessage());
        }
        Assertions.assertEquals(200, response.code(), "Юзеры не найдены - " + response.message());
        return response.body() != null
                ? response.body().stream().map(x -> x.addTestData(new TestData(DEFAULT_PASSWORD))).toList()
                : Collections.emptyList();
    }

    @Override
    @Step("Создать входящее предложение дружбы от пользователя {1} для пользователя {0} с помощью REST api")
    public void createIncomeInvitations(UserdataUserJson income, String... outcomesUsername) {
        Arrays.stream(outcomesUsername).forEach(outcomeUsername -> {
            final Response<UserdataUserJson> response;
            UserdataUserJson outcome;
            try {
                outcome = create(outcomeUsername, RUB, DEFAULT_PASSWORD);
                response = userdataApi.sendInvitation(outcome.username(), income.username()).execute();
            } catch (IOException e) {
                throw new AssertionError(e.getMessage());
            }
            Assertions.assertEquals(200, response.code(), response.message());
            if (income.testData() == null) {
                income.addTestData(new TestData(DEFAULT_PASSWORD));
            }
            income.testData().income().add(outcome);
        });
    }

    @Override
    @Step("Создать исходящее предложение дружбы от пользователя {0} для пользователя {1} с помощью REST api")
    public void createOutcomeInvitations(UserdataUserJson outcome, String... incomesUsername) {
        Arrays.stream(incomesUsername).forEach(incomeUsername -> {
            final Response<UserdataUserJson> response;
            UserdataUserJson income;
            try {
                income = create(incomeUsername, RUB, DEFAULT_PASSWORD);
                response = userdataApi.sendInvitation(outcome.username(), income.username()).execute();
            } catch (IOException e) {
                throw new AssertionError(e.getMessage());
            }
            Assertions.assertEquals(200, response.code(), response.message());
            if (outcome.testData() == null) {
                outcome.addTestData(new TestData(DEFAULT_PASSWORD));
            }
            income.testData().income().add(outcome);
        });
    }

    @Override
    @Step("Создать дружбу между пользователями {0} и {1} с помощью REST api")
    public void createFriends(UserdataUserJson currentUser, String... friendsUsername) {
        Arrays.stream(friendsUsername).forEach(friendUsername -> {
            final Response<UserdataUserJson> response;
            UserdataUserJson friend;
            try {
                friend = create(friendUsername, RUB, DEFAULT_PASSWORD);
                userdataApi.sendInvitation(currentUser.username(), friend.username()).execute();
                response = userdataApi.acceptInvitation(friend.username(), currentUser.username()).execute();
            } catch (IOException e) {
                throw new AssertionError(e.getMessage());
            }
            Assertions.assertEquals(200, response.code(), response.message());
            currentUser.testData().friends().add(friend);
        });
    }

    @Override
    public Optional<UserdataUserJson> findById(UUID id) {
        throw new InternalError("Метод findById не реализован в api проекта");
    }

    @Override
    public void remove(UserdataUserJson userdataUserEntity) {
        throw new InternalError("Метод remove не реализован в api проекта");
    }

}

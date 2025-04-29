package guru.qa.niffler.service;

import guru.qa.niffler.model.CurrencyValues;
import guru.qa.niffler.model.rest.UserdataUserJson;
import guru.qa.niffler.service.userdata.UserdataApiClient;
import guru.qa.niffler.service.userdata.UserdataDBSpringRepositoryClient;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
public interface UsersClient {

    static UsersClient getInstance() {
        return "api".equals(System.getProperty("client.impl"))
                ? new UserdataApiClient()
                : new UserdataDBSpringRepositoryClient();
    }

    @Nonnull
    UserdataUserJson create(String username, CurrencyValues currencyValue, String password);

    @Nonnull
    Optional<UserdataUserJson> findById(UUID id);

    @Nonnull
    Optional<UserdataUserJson> findByUsername(String username);

    @Nonnull
    UserdataUserJson update(UserdataUserJson user);

    void remove(UserdataUserJson userdataUserEntity);

    /* создать запрос в друзья от кого то */
    void createIncomeInvitations(UserdataUserJson income, String... outcomesUsername);

    /* создать запрос в друзья к кому то */
    void createOutcomeInvitations(UserdataUserJson outcome, String... incomesUsername);

    void createFriends(UserdataUserJson currentUser, String... friendsUsername);
}

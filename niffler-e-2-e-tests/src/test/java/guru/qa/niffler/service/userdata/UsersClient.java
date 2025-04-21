package guru.qa.niffler.service.userdata;

import guru.qa.niffler.model.CurrencyValues;
import guru.qa.niffler.model.UserdataUserJson;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
public interface UsersClient {

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

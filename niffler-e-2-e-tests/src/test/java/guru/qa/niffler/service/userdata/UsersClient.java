package guru.qa.niffler.service.userdata;

import guru.qa.niffler.model.CurrencyValues;
import guru.qa.niffler.model.UserdataUserJson;

import java.util.Optional;
import java.util.UUID;

public interface UsersClient {

    UserdataUserJson create(String username, CurrencyValues currencyValue, String password);

    Optional<UserdataUserJson> findById(UUID id);

    Optional<UserdataUserJson> findByUsername(String username);

    UserdataUserJson update(UserdataUserJson user);

    void remove(UserdataUserJson userdataUserEntity);

    /* создать запрос в друзья от кого то */
    void createIncomeInvitations(UserdataUserJson income, String... outcomesUsername);

    /* создать запрос в друзья к кому то */
    void createOutcomeInvitations(UserdataUserJson outcome, String... incomesUsername);

    void createFriends(UserdataUserJson currentUser, String... friendsUsername);
}

package guru.qa.niffler.service.userdata;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.Authority;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import guru.qa.niffler.data.entity.userdata.FriendshipStatus;
import guru.qa.niffler.data.entity.userdata.UserdataUserEntity;
import guru.qa.niffler.data.repository.auth.AuthUserRepository;
import guru.qa.niffler.data.repository.auth.impl.AuthUserRepositoryJdbc;
import guru.qa.niffler.data.repository.userdata.UserdataRepository;
import guru.qa.niffler.data.repository.userdata.impl.UserdataRepositoryJdbc;
import guru.qa.niffler.data.template.JdbcTransactionTemplate;
import guru.qa.niffler.data.template.XaTransactionTemplate;
import guru.qa.niffler.model.CurrencyValues;
import guru.qa.niffler.model.UserdataUserJson;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Connection;
import java.util.*;

public class UserdataDBRepositoryClient implements UsersClient {
    private static final Config CFG = Config.getInstance();
    private static final PasswordEncoder pe = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    private final UserdataRepository udUserRepository;
    private final AuthUserRepository authUserRepository;
    private final JdbcTransactionTemplate txTemplate;
    private final XaTransactionTemplate xaTxTemplate;

    public UserdataDBRepositoryClient() {
        udUserRepository = new UserdataRepositoryJdbc();
        authUserRepository = new AuthUserRepositoryJdbc();
        txTemplate = new JdbcTransactionTemplate(CFG.userdataJdbcUrl());
        xaTxTemplate = new XaTransactionTemplate(CFG.userdataJdbcUrl(), CFG.authJdbcUrl());
    }

    public UserdataUserJson create(String username, CurrencyValues currencyValue, String password) {
        return xaTxTemplate.execute(Connection.TRANSACTION_READ_UNCOMMITTED,
                () -> UserdataUserJson.fromEntity(
                        createNewUser(username, currencyValue, password), null));
    }

    @Override
    public UserdataUserJson update(UserdataUserJson user) {
        return xaTxTemplate.execute(() ->
                UserdataUserJson.fromEntity(udUserRepository
                        .update(UserdataUserEntity.fromJson(user)), user.friendState()));
    }

    public Optional<UserdataUserJson> findByUsername(String username) {
        return txTemplate.execute(Connection.TRANSACTION_READ_COMMITTED,
                () -> udUserRepository.findByUsername(username)
                        .map(x -> UserdataUserJson.fromEntity(
                                x, null)));
    }

    public Optional<UserdataUserJson> findById(UUID id) {
        return txTemplate.execute(Connection.TRANSACTION_READ_COMMITTED,
                () -> udUserRepository.findById(id)
                        .map(x -> UserdataUserJson.fromEntity(
                                x, null)));
    }

    /* создать запрос в друзья от кого то */
    public void createIncomeInvitations(UserdataUserJson income, String... outcomesUsername) {
        UserdataUserEntity incomeEntity = udUserRepository.findById(income.id()).orElseThrow();
        for (String outcomeUsername : outcomesUsername) {
            xaTxTemplate.execute(() -> {
                UserdataUserEntity outcomeEntity = createNewUser(outcomeUsername, CurrencyValues.RUB, "12345");
                udUserRepository.sendInvitation(
                        FriendshipStatus.PENDING,
                        outcomeEntity,
                        incomeEntity);
            });
        }
    }

    /* создать запрос в друзья к кому то */
    public void createOutcomeInvitations(UserdataUserJson outcome, String... incomesUsername) {
        UserdataUserEntity outcomeEntity = udUserRepository.findById(outcome.id()).orElseThrow();
        xaTxTemplate.execute(() -> {
            UserdataUserEntity[] incomes = new UserdataUserEntity[incomesUsername.length];
            for (int x = 0; x < incomesUsername.length; x++) {
                int y = x;
                incomes[x] = createNewUser(incomesUsername[y], CurrencyValues.RUB, "12345");
            }
            udUserRepository.sendInvitation(
                    FriendshipStatus.PENDING,
                    outcomeEntity,
                    incomes);
            return null;
        });
    }

    public void createFriends(UserdataUserJson currentUser, String... friendsUsername) {
        UserdataUserEntity currentEntity = udUserRepository.findById(currentUser.id()).orElseThrow();
        xaTxTemplate.execute(() -> {
            UserdataUserEntity[] friends = new UserdataUserEntity[friendsUsername.length];
            for (int x = 0; x < friendsUsername.length; x++) {
                int y = x;
                friends[x] = createNewUser(friendsUsername[y], CurrencyValues.RUB, "12345");
            }
            udUserRepository.addFriend(
                    currentEntity,
                    friends);
            return null;
        });
    }

    public void remove(UserdataUserJson userdataUserJson) {
        xaTxTemplate.execute(Connection.TRANSACTION_REPEATABLE_READ,
                () -> {
                    udUserRepository.remove(
                            UserdataUserEntity.fromJson(userdataUserJson));
                    authUserRepository.findByUsername(userdataUserJson.username())
                            .ifPresent(authUserRepository::remove);
                });
    }

    private UserdataUserEntity createNewUser(String username, CurrencyValues currencyValue, String password) {
        return udUserRepository.findByUsername(username).orElseGet(() -> {
            AuthUserEntity authUser = setAuthUserEntity(username, password);
            authUserRepository.create(authUser);
            return udUserRepository.create(setUdUserEntity(username, currencyValue));
        });
    }

    private UserdataUserEntity setUdUserEntity(String username, CurrencyValues currencyValue) {
        UserdataUserEntity user = new UserdataUserEntity();
        user.setUsername(username);
        user.setCurrency(currencyValue);
        return user;
    }

    private AuthUserEntity setAuthUserEntity(String username, String password) {
        AuthUserEntity authUser = new AuthUserEntity();
        authUser.setUsername(username);
        authUser.setPassword(pe.encode(password));
        authUser.setEnabled(true);
        authUser.setAccountNonExpired(true);
        authUser.setAccountNonLocked(true);
        authUser.setCredentialsNonExpired(true);
        authUser.setAuthorities(
                Arrays.stream(Authority.values())
                        .map(a -> {
                                    AuthorityEntity ae = new AuthorityEntity();
                                    ae.setUser(authUser);
                                    ae.setAuthority(a);
                                    return ae;
                                }
                        ).toList());
        return authUser;
    }
}
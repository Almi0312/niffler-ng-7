package guru.qa.niffler.service.userdata;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.Authority;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import guru.qa.niffler.data.entity.userdata.FriendshipStatus;
import guru.qa.niffler.data.entity.userdata.UserdataUserEntity;
import guru.qa.niffler.data.repository.auth.AuthUserRepository;
import guru.qa.niffler.data.repository.auth.impl.AuthUserSpringRepositoryJdbc;
import guru.qa.niffler.data.repository.userdata.UserdataRepository;
import guru.qa.niffler.data.repository.userdata.impl.UserdataSpringRepositoryJdbc;
import guru.qa.niffler.data.jdbc.DataSources;
import guru.qa.niffler.data.template.XaTransactionTemplate;
import guru.qa.niffler.model.CurrencyValues;
import guru.qa.niffler.model.rest.UserdataUserJson;
import guru.qa.niffler.service.UsersClient;
import org.jetbrains.annotations.NotNull;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.sql.Connection;
import java.util.*;

@ParametersAreNonnullByDefault
public class UserdataDBSpringRepositoryClient implements UsersClient {
    private static final Config CFG = Config.getInstance();
    private static final PasswordEncoder pe = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    private final UserdataRepository udUserRepository;
    private final AuthUserRepository authUserRepository;
    private final TransactionTemplate txTemplate;
    private final XaTransactionTemplate xaTxTemplate;

    public UserdataDBSpringRepositoryClient() {
        udUserRepository = new UserdataSpringRepositoryJdbc();
        authUserRepository = new AuthUserSpringRepositoryJdbc();
        txTemplate = new TransactionTemplate(
                new JdbcTransactionManager(DataSources.dataSource(CFG.userdataJdbcUrl())));
        xaTxTemplate = new XaTransactionTemplate(CFG.userdataJdbcUrl(), CFG.authJdbcUrl());
    }

    @Override
    public @Nonnull UserdataUserJson create(String username, CurrencyValues currencyValue, String password) {
        txTemplate.setIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
        return xaTxTemplate.execute(
                () -> UserdataUserJson.fromEntity(createNewUser(username, currencyValue, password), null));
    }

    @Override
    public @Nonnull UserdataUserJson update(UserdataUserJson user) {
        txTemplate.setIsolationLevel(Connection.TRANSACTION_REPEATABLE_READ);
        return Objects.requireNonNull(txTemplate.execute(con ->
                UserdataUserJson.fromEntity(udUserRepository.update(UserdataUserEntity.fromJson(user)), null)));
    }

    /* создать запрос в друзья от кого то */
    @Override
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
    @Override
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

    @Override
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

    @Override
    public @Nonnull Optional<UserdataUserJson> findById(UUID id) {
        txTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        return txTemplate.execute(con ->
                udUserRepository.findById(id)
                        .map(user -> UserdataUserJson.fromEntity(user, null)));
    }

    @Override
    public @Nonnull Optional<UserdataUserJson> findByUsername(String username) {
        txTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        return txTemplate.execute(con ->
                udUserRepository.findByUsername(username)
                        .map(user -> UserdataUserJson.fromEntity(user, null)));
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

    @NotNull
    @Override
    public List<UserdataUserJson> findAllFriendshipByUsername(String username, String searchQuery) {
        return List.of();
    }
}
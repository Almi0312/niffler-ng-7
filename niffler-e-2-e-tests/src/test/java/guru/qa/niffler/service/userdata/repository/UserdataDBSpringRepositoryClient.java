package guru.qa.niffler.service.userdata.repository;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.Authority;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import guru.qa.niffler.data.entity.userdata.FriendshipStatus;
import guru.qa.niffler.data.entity.userdata.UserdataUserEntity;
import guru.qa.niffler.data.repository.auth.AuthUserRepository;
import guru.qa.niffler.data.repository.auth.impl.AuthUserSpringRepositoryJdbc;
import guru.qa.niffler.data.repository.userdata.UserdataUserRepository;
import guru.qa.niffler.data.repository.userdata.impl.UserdataSpringRepositoryJdbc;
import guru.qa.niffler.data.template.DataSources;
import guru.qa.niffler.data.template.XaTransactionTemplate;
import guru.qa.niffler.model.UserdataUserJson;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public class UserdataDBSpringRepositoryClient {
    private static final Config CFG = Config.getInstance();
    private static final PasswordEncoder pe = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    private final UserdataUserRepository udUserRepository;
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

    public UserdataUserJson create(UserdataUserJson user) {
        return UserdataUserJson.fromEntity(xaTxTemplate.execute(Connection.TRANSACTION_READ_UNCOMMITTED,
                () -> {
                    AuthUserEntity authUser = new AuthUserEntity();
                    authUser.setUsername(user.username());
                    authUser.setPassword(pe.encode("12345"));
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
                    authUserRepository.create(authUser);
                    return udUserRepository.create(UserdataUserEntity.fromJson(user));
                }), user.friendState());
    }

    /* создать запрос в друзья от кого то */
    public void createIncomeInvitations(UserdataUserEntity income, UserdataUserEntity... outcomes) {
        txTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
        txTemplate.executeWithoutResult(con ->
                Arrays.stream(outcomes).forEach(request ->
                        udUserRepository.createOutcomeInvitations(FriendshipStatus.PENDING, request, income)));
    }

    /* создать запрос в друзья к кому то */
    public void createOutcomeInvitations(UserdataUserEntity outcome, UserdataUserEntity... incomes) {
        txTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
        txTemplate.executeWithoutResult(con ->
                udUserRepository.createOutcomeInvitations(FriendshipStatus.PENDING, outcome, incomes));
    }

    public void createFriends(UserdataUserEntity currentUser, UserdataUserEntity... potentialFriends) {
        txTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
        txTemplate.executeWithoutResult(con -> {
            udUserRepository.createOutcomeInvitations(FriendshipStatus.ACCEPTED, currentUser, potentialFriends);
            Arrays.stream(potentialFriends).forEach(addressee ->
                    udUserRepository.createOutcomeInvitations(
                            FriendshipStatus.ACCEPTED, addressee, currentUser));
        });
    }

    public Optional<UserdataUserJson> findById(UUID id) {
        txTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        return txTemplate.execute(con ->
                udUserRepository.findById(id)
                        .map(user -> UserdataUserJson.fromEntity(user, null)));
    }

    public Optional<UserdataUserJson> findByUsername(String username) {
        txTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        return txTemplate.execute(con ->
                udUserRepository.findByUsername(username)
                        .map(user -> UserdataUserJson.fromEntity(user, null)));
    }

    public void delete(UserdataUserJson userdataUserJson) {
        xaTxTemplate.execute(Connection.TRANSACTION_SERIALIZABLE,
                () -> {
                    udUserRepository.delete(
                            UserdataUserEntity.fromJson(userdataUserJson));
                    authUserRepository.findByUsername(userdataUserJson.username())
                            .ifPresent(authUserRepository::deleteById);
                });
    }
}
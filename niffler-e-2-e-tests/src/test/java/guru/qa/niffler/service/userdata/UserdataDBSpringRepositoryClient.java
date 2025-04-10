package guru.qa.niffler.service.userdata;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.Authority;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import guru.qa.niffler.data.entity.userdata.FriendshipStatus;
import guru.qa.niffler.data.entity.userdata.UserdataUserEntity;
import guru.qa.niffler.data.repository.auth.AuthUserRepository;
import guru.qa.niffler.data.repository.auth.impl.AuthUserRepositoryJdbc;
import guru.qa.niffler.data.repository.auth.impl.AuthUserSpringRepositoryJdbc;
import guru.qa.niffler.data.repository.userdata.UserdataUserRepository;
import guru.qa.niffler.data.repository.userdata.impl.UserdataUserRepositoryJdbc;
import guru.qa.niffler.data.repository.userdata.impl.UserdataUserSpringRepositoryJdbc;
import guru.qa.niffler.data.template.JdbcTransactionTemplate;
import guru.qa.niffler.data.template.XaTransactionTemplate;
import guru.qa.niffler.model.UserdataUserJson;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public class UserdataDBSpringRepositoryClient {
    private static final Config CFG = Config.getInstance();
    private static final PasswordEncoder pe = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    private final UserdataUserRepository udUserRepository;
    private final AuthUserRepository authUserRepository;
    private final JdbcTransactionTemplate txTemplate;
    private final XaTransactionTemplate xaTxTemplate;

    public UserdataDBSpringRepositoryClient() {
        udUserRepository = new UserdataUserSpringRepositoryJdbc();
        authUserRepository = new AuthUserSpringRepositoryJdbc();
        txTemplate = new JdbcTransactionTemplate(CFG.userdataJdbcUrl());
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

    public void createRequester(UserdataUserEntity requester, UserdataUserEntity... addressees) {
        txTemplate.execute(Connection.TRANSACTION_READ_UNCOMMITTED,
                con -> udUserRepository.createRequester(FriendshipStatus.PENDING, requester, addressees));
    }

    public void createAddressee(UserdataUserEntity addressee, UserdataUserEntity... requesters) {
        txTemplate.execute(Connection.TRANSACTION_READ_UNCOMMITTED,
                con -> udUserRepository.createAddressee(addressee, requesters));
    }

    public void createFriends(UserdataUserEntity requester, UserdataUserEntity... addressees) {
        txTemplate.execute(Connection.TRANSACTION_READ_COMMITTED,
                con -> {
                    udUserRepository.createRequester(FriendshipStatus.ACCEPTED, requester, addressees);
                    Arrays.stream(addressees).forEach(addressee ->
                            udUserRepository.createRequester(
                                    FriendshipStatus.ACCEPTED, addressee, requester));
                });
    }

    public Optional<UserdataUserJson> findById(UUID id) {
        return txTemplate.execute(Connection.TRANSACTION_READ_COMMITTED,
                () -> udUserRepository.findById(id)
                        .map(x -> UserdataUserJson.fromEntity(
                                x, null)));
    }

    public Optional<UserdataUserJson> findByUsername(String username) {
        return txTemplate.execute(Connection.TRANSACTION_READ_COMMITTED,
                () -> udUserRepository.findByUsername(username)
                        .map(x -> UserdataUserJson.fromEntity(
                                x, null)));
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
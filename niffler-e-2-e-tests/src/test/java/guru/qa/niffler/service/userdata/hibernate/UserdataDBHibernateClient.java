package guru.qa.niffler.service.userdata.hibernate;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.Authority;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import guru.qa.niffler.data.entity.userdata.UserdataUserEntity;
import guru.qa.niffler.data.repository.auth.AuthUserRepository;
import guru.qa.niffler.data.repository.auth.impl.AuthUserRepositoryJdbc;
import guru.qa.niffler.data.repository.userdata.UserdataUserRepository;
import guru.qa.niffler.data.repository.userdata.impl.UserdataRepositoryJdbc;
import guru.qa.niffler.data.template.JdbcTransactionTemplate;
import guru.qa.niffler.data.template.XaTransactionTemplate;
import guru.qa.niffler.model.CurrencyValues;
import guru.qa.niffler.model.UserdataUserJson;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public class UserdataDBHibernateClient {
    private static final Config CFG = Config.getInstance();
    private static final PasswordEncoder pe = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    private final UserdataUserRepository udUserRepository;
    private final AuthUserRepository authUserRepository;
    private final JdbcTransactionTemplate txTemplate;
    private final XaTransactionTemplate xaTxTemplate;

    public UserdataDBHibernateClient() {
        udUserRepository = new UserdataRepositoryJdbc();
        authUserRepository = new AuthUserRepositoryJdbc();
        txTemplate = new JdbcTransactionTemplate(CFG.userdataJdbcUrl());
        xaTxTemplate = new XaTransactionTemplate(CFG.userdataJdbcUrl(), CFG.authJdbcUrl());
    }

    public UserdataUserJson create(String username, String password) {
        return xaTxTemplate.execute(Connection.TRANSACTION_READ_UNCOMMITTED,
                        () -> {
                            AuthUserEntity authUser = authUserEntity(username, password);
                            authUserRepository.create(authUser);
                            return UserdataUserJson.fromEntity(
                                    udUserRepository.create(userdataEntity(username)), null);
                        });
    }

    private UserdataUserEntity userdataEntity(String username) {
        UserdataUserEntity user = new UserdataUserEntity();
        user.setUsername(username);
        user.setCurrency(CurrencyValues.RUB);
        return user;
    }

    private AuthUserEntity authUserEntity(String username, String password) {
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
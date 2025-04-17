package guru.qa.niffler.service.userdata.dao;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.auth.AuthUserDAO;
import guru.qa.niffler.data.dao.auth.AuthorityDAO;
import guru.qa.niffler.data.dao.auth.impl.spring.AuthUserDAOSpringJdbc;
import guru.qa.niffler.data.dao.auth.impl.spring.AuthorityDAOSpringJdbc;
import guru.qa.niffler.data.dao.userdata.UserdataUserDAO;
import guru.qa.niffler.data.dao.userdata.impl.UserdataUserDAOSpringJdbc;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.Authority;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import guru.qa.niffler.data.entity.userdata.UserdataUserEntity;
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

public class UserdataDBSpringClient {
    private static final Config CFG = Config.getInstance();
    private static final PasswordEncoder pe = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    private final UserdataUserDAO udUserDAO;
    private final AuthUserDAO authUserDAO;
    private final AuthorityDAO authorityDAO;
    private final TransactionTemplate txTemplate;
    private final XaTransactionTemplate xaTxTemplate;

    public UserdataDBSpringClient() {
        udUserDAO = new UserdataUserDAOSpringJdbc();
        authUserDAO = new AuthUserDAOSpringJdbc();
        authorityDAO = new AuthorityDAOSpringJdbc();
        txTemplate = new TransactionTemplate(
                new JdbcTransactionManager(DataSources.dataSource(CFG.userdataJdbcUrl())));
        xaTxTemplate = new XaTransactionTemplate(CFG.userdataJdbcUrl(), CFG.authJdbcUrl());
    }

    public UserdataUserJson create(UserdataUserJson user) {
        return xaTxTemplate.execute(Connection.TRANSACTION_READ_UNCOMMITTED,
                () -> {
                    AuthUserEntity authUser = new AuthUserEntity();
                    authUser.setUsername(user.username());
                    authUser.setPassword(pe.encode("12345"));
                    authUser.setEnabled(true);
                    authUser.setAccountNonExpired(true);
                    authUser.setAccountNonLocked(true);
                    authUser.setCredentialsNonExpired(true);
                    authUser.setId(
                            authUserDAO.create(authUser).getId());
                    authorityDAO.create(
                            Arrays.stream(Authority.values())
                                    .map(a -> {
                                                AuthorityEntity ae = new AuthorityEntity();
                                                ae.setUser(authUser);
                                                ae.setAuthority(a);
                                                return ae;
                                            }
                                    ).toArray(AuthorityEntity[]::new));
                    return UserdataUserJson.fromEntity(udUserDAO
                            .create(
                                    UserdataUserEntity.fromJson(user)), null);
                });
    }

    public Optional<UserdataUserJson> findById(UUID id) {
        txTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        return txTemplate.execute(con ->
                udUserDAO.findById(id)
                        .map(x -> UserdataUserJson.fromEntity(x, null)));
    }

    public Optional<UserdataUserJson> findByUsername(String username) {
        txTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        return txTemplate.execute(con ->
                udUserDAO.findByUsername(username)
                        .map(x -> UserdataUserJson.fromEntity(x, null)));
    }

    public void delete(UserdataUserJson userdataUserJson) {
        xaTxTemplate.execute(Connection.TRANSACTION_SERIALIZABLE,
                () -> {
                    udUserDAO.delete(
                            UserdataUserEntity.fromJson(userdataUserJson));
                    authUserDAO.findByUsername(userdataUserJson.username())
                            .ifPresent(entity -> {
                                authorityDAO.delete(entity);
                                authUserDAO.delete(entity);
                            });
                });
    }
}

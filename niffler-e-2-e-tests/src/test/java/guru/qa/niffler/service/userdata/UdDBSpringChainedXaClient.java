package guru.qa.niffler.service.userdata;

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
import guru.qa.niffler.model.UserdataUserJson;
import org.springframework.data.transaction.ChainedTransactionManager;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static guru.qa.niffler.data.template.DataSources.dataSource;

public class UdDBSpringChainedXaClient {
    private static final Config CFG = Config.getInstance();
    private static final PasswordEncoder pe = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    private final UserdataUserDAO udUserDAO;
    private final AuthUserDAO authUserDAO;
    private final AuthorityDAO authorityDAO;
    private final TransactionTemplate chainedTemplate;

    public UdDBSpringChainedXaClient() {
        udUserDAO = new UserdataUserDAOSpringJdbc();
        authUserDAO = new AuthUserDAOSpringJdbc();
        authorityDAO = new AuthorityDAOSpringJdbc();
        chainedTemplate = new TransactionTemplate(
                new ChainedTransactionManager(
                        new JdbcTransactionManager(dataSource(CFG.authJdbcUrl())),
                        new JdbcTransactionManager(dataSource(CFG.userdataJdbcUrl()))
                )
        );
    }

    public UserdataUserJson create(UserdataUserJson user) {
        chainedTemplate.setIsolationLevel(Connection.TRANSACTION_READ_UNCOMMITTED);
        return chainedTemplate.execute(connection -> {
                    AuthUserEntity authUser = new AuthUserEntity();
                    authUser.setUsername(user.username());
                    authUser.setPassword(pe.encode("12345"));
                    authUser.setEnabled(true);
                    authUser.setAccountNonExpired(true);
                    authUser.setAccountNonLocked(true);
                    authUser.setCredentialsNonExpired(true);
                    authUserDAO.create(authUser);
                    authorityDAO.create(
                            Arrays.stream(Authority.values())
                                    .map(a -> {
                                                AuthorityEntity ae = new AuthorityEntity();
                                                ae.setUser(authUser);
                                                ae.setAuthority(a);
                                                return ae;
                                            }
                                    ).toArray(AuthorityEntity[]::new));
                    //тут падение транзакции и проверка rollback
                    if (user.username().equals("failUser")) {
                        throw new RuntimeException("Искусственная ошибка в authUserDAO");
                    }
                    return UserdataUserJson.fromEntity(udUserDAO
                            .create(
                                    UserdataUserEntity.fromJson(user)), null);
                }
        );
    }

    public Optional<UserdataUserJson> findById(UUID id) {
        chainedTemplate.setIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
        return chainedTemplate.execute(connection ->
                udUserDAO.findById(id)
                        .map(x ->
                                UserdataUserJson.fromEntity(x, null)));
    }

    public Optional<UserdataUserJson> findByUsername(String username) {
        chainedTemplate.setIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
        return chainedTemplate.execute(connection ->
                udUserDAO.findByUsername(username)
                        .map(x -> UserdataUserJson.fromEntity(x, null)));
    }

    public void delete(UserdataUserJson userdataUserJson) {
        chainedTemplate.setIsolationLevel(Connection.TRANSACTION_SERIALIZABLE);
        chainedTemplate.executeWithoutResult(connection -> {
            udUserDAO.delete(
                    UserdataUserEntity.fromJson(userdataUserJson));
            authUserDAO.findByUsername(userdataUserJson.username())
                    .ifPresent(entity -> {
                        authorityDAO.delete(entity);
                        authUserDAO.deleteByUsername(entity);
                    });
        });
    }

}
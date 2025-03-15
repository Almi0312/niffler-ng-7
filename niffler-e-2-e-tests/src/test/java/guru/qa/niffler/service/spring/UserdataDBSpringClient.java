package guru.qa.niffler.service.spring;

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
import guru.qa.niffler.data.template.JdbcTransactionTemplate;
import guru.qa.niffler.data.template.XaTransactionTemplate;
import guru.qa.niffler.model.UserdataUserJson;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.util.Arrays;
import java.util.UUID;

public class UserdataDBSpringClient {
    private static final Config CFG = Config.getInstance();
    private static final PasswordEncoder pe = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    private final UserdataUserDAO udUserDAO;
    private final AuthUserDAO authUserDAO;
    private final AuthorityDAO authorityDAO;
    private final JdbcTransactionTemplate txTemplate;
    private final XaTransactionTemplate xaTxTemplate;

    public UserdataDBSpringClient() {
        udUserDAO = new UserdataUserDAOSpringJdbc();
        authUserDAO = new AuthUserDAOSpringJdbc();
        authorityDAO = new AuthorityDAOSpringJdbc();
        txTemplate = new JdbcTransactionTemplate(CFG.userdataJdbcUrl());
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
                    authUserDAO.create(authUser);
                    authorityDAO.create(
                            Arrays.stream(Authority.values())
                                    .map(a -> {
                                                AuthorityEntity ae = new AuthorityEntity();
                                                ae.setUserId(authUser.getId());
                                                ae.setAuthority(a);
                                                return ae;
                                            }
                                    ).toArray(AuthorityEntity[]::new));
                    return null;
                },
                () -> UserdataUserJson.fromEntity(udUserDAO
                        .create(
                                UserdataUserEntity.fromJson(user)), null));
    }

    public @Nullable UserdataUserJson findById(UUID id) {
        return txTemplate.execute(() ->
                udUserDAO.findById(id)
                        .map(x ->
                                UserdataUserJson.fromEntity(x, null))
                        .orElse(null));
    }

    public @Nullable UserdataUserJson findByUsername(String username) {
        return txTemplate.execute(() ->
                udUserDAO.findByUsername(username)
                        .map(x -> UserdataUserJson.fromEntity(x, null))
                        .orElse(null));
    }

    public void delete(UserdataUserJson userdataUserJson) {
        xaTxTemplate.execute(Connection.TRANSACTION_SERIALIZABLE,
                (() -> udUserDAO.delete(
                        UserdataUserEntity.fromJson(userdataUserJson)))
                ,
                () -> {
                    AuthUserEntity entity = authUserDAO.findByUsername(userdataUserJson.username()).orElse(null);
                    if (entity != null) {
                        authorityDAO.delete(entity);
                        authUserDAO.deleteByUsername(entity);
                    }
                });
    }

}
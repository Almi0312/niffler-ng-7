package guru.qa.niffler.service.default_jdbc;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.auth.AuthUserDAO;
import guru.qa.niffler.data.dao.auth.AuthorityDAO;
import guru.qa.niffler.data.dao.auth.impl.default_jdbc.AuthUserDAOJdbc;
import guru.qa.niffler.data.dao.auth.impl.default_jdbc.AuthorityDAOJdbc;
import guru.qa.niffler.data.dao.userdata.UserdataUserDAO;
import guru.qa.niffler.data.dao.userdata.impl.UserdataUserDAOJdbc;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import guru.qa.niffler.data.entity.userdata.UserdataUserEntity;
import guru.qa.niffler.data.entity.auth.Authority;
import guru.qa.niffler.data.template.JdbcTransactionTemplate;
import guru.qa.niffler.data.template.XaTransactionTemplate;
import guru.qa.niffler.model.UserdataUserJson;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.util.Arrays;
import java.util.UUID;

public class UserdataDBClient {
    private static final Config CFG = Config.getInstance();
    private static final PasswordEncoder pe = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    private final UserdataUserDAO udUserDAO;
    private final AuthUserDAO authUserDAO;
    private final AuthorityDAO authorityDAO;
    private final JdbcTransactionTemplate txTemplate;
    private final XaTransactionTemplate xaTxTemplate;

    public UserdataDBClient() {
        udUserDAO = new UserdataUserDAOJdbc();
        authUserDAO = new AuthUserDAOJdbc();
        authorityDAO = new AuthorityDAOJdbc();
        txTemplate = new JdbcTransactionTemplate(CFG.userdataJdbcUrl());
        xaTxTemplate = new XaTransactionTemplate(CFG.userdataJdbcUrl(), CFG.authJdbcUrl());
    }

    public UserdataUserJson create(UserdataUserJson user) {
        return UserdataUserJson.fromEntity(
                xaTxTemplate.execute(Connection.TRANSACTION_READ_UNCOMMITTED,
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
                        () -> {
                            UserdataUserEntity ue = new UserdataUserEntity();
                            ue.setUsername(user.username());
                            ue.setFullname(user.fullname());
                            ue.setCurrency(user.currency());
                            udUserDAO.create(ue);
                            return ue;
                        }), null);
    }

    public @Nullable UserdataUserJson findById(UUID id) {
        return txTemplate.execute(Connection.TRANSACTION_READ_COMMITTED,
                () -> udUserDAO.findById(id)
                        .map(x -> UserdataUserJson.fromEntity(
                                x, null)).orElse(null));
    }

    public @Nullable UserdataUserJson findByUsername(String username) {
        return txTemplate.execute(Connection.TRANSACTION_READ_COMMITTED,
                () -> udUserDAO.findByUsername(username)
                        .map(x -> UserdataUserJson.fromEntity(
                                x, null)).orElse(null));
    }

    public void delete(UserdataUserJson userdataUserJson) {
        xaTxTemplate.execute(Connection.TRANSACTION_SERIALIZABLE,
                () -> udUserDAO.delete(
                        UserdataUserEntity.fromJson(userdataUserJson)),
                () -> authUserDAO.findByUsername(userdataUserJson.username())
                        .ifPresent(authUser -> {
                            authorityDAO.delete(authUser);
                            authUserDAO.deleteByUsername(authUser);
                        }));
    }
}
package guru.qa.niffler.service.auth;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.auth.AuthUserDAO;
import guru.qa.niffler.data.dao.auth.AuthorityDAO;
import guru.qa.niffler.data.dao.auth.impl.spring.AuthUserDAOSpringJdbc;
import guru.qa.niffler.data.dao.auth.impl.spring.AuthorityDAOSpringJdbc;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import guru.qa.niffler.data.template.JdbcTransactionTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AuthUserDBSpringClient {
    private static final Config CFG = Config.getInstance();

    private final AuthUserDAO authUserDAO;
    private final AuthorityDAO authorityDAO;
    private final JdbcTransactionTemplate txTemplate;

    public AuthUserDBSpringClient() {
        authUserDAO = new AuthUserDAOSpringJdbc();
        authorityDAO = new AuthorityDAOSpringJdbc();
        txTemplate = new JdbcTransactionTemplate(CFG.userdataJdbcUrl());
    }

    public Optional<AuthUserEntity> findUserById(UUID id) {
        return txTemplate.execute(
                () -> authUserDAO.findById(id));
    }

    public Optional<AuthUserEntity> findUserByUsername(String username) {
        return txTemplate.execute(
                () -> authUserDAO.findByUsername(username));
    }

    public List<AuthorityEntity> findAuthorityByUserId(AuthUserEntity authUser) {
        return txTemplate.execute(
                () -> authorityDAO.findByAuthUserId(authUser));
    }

}

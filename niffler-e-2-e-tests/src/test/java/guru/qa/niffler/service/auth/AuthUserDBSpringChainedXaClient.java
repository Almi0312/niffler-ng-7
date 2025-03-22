package guru.qa.niffler.service.auth;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.auth.AuthUserDAO;
import guru.qa.niffler.data.dao.auth.AuthorityDAO;
import guru.qa.niffler.data.dao.auth.impl.spring.AuthUserDAOSpringJdbc;
import guru.qa.niffler.data.dao.auth.impl.spring.AuthorityDAOSpringJdbc;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import guru.qa.niffler.data.template.JdbcTransactionTemplate;
import org.springframework.data.transaction.ChainedTransactionManager;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static guru.qa.niffler.data.template.DataSources.dataSource;

public class AuthUserDBSpringChainedXaClient {
    private static final Config CFG = Config.getInstance();

    private final AuthUserDAO authUserDAO;
    private final AuthorityDAO authorityDAO;
    private final TransactionTemplate chainedTemplate;

    public AuthUserDBSpringChainedXaClient() {
        authUserDAO = new AuthUserDAOSpringJdbc();
        authorityDAO = new AuthorityDAOSpringJdbc();
        chainedTemplate = new TransactionTemplate(
                new ChainedTransactionManager(
                        new JdbcTransactionManager(dataSource(CFG.authJdbcUrl()))
                )
        );
    }

    public Optional<AuthUserEntity> findUserById(UUID id) {
        return chainedTemplate.execute(
                con -> authUserDAO.findById(id));
    }

    public Optional<AuthUserEntity> findUserByUsername(String username) {
        return chainedTemplate.execute(
                con -> authUserDAO.findByUsername(username));
    }

    public List<AuthorityEntity> findAuthorityByUserId(AuthUserEntity authUser) {
        return chainedTemplate.execute(
                con -> authorityDAO.findByAuthUserId(authUser));
    }

}

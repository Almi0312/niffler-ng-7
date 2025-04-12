package guru.qa.niffler.service.auth.dao;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.auth.AuthUserDAO;
import guru.qa.niffler.data.dao.auth.AuthorityDAO;
import guru.qa.niffler.data.dao.auth.impl.spring.AuthUserDAOSpringJdbc;
import guru.qa.niffler.data.dao.auth.impl.spring.AuthorityDAOSpringJdbc;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import guru.qa.niffler.data.template.DataSources;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AuthUserDBSpringClient {
    private static final Config CFG = Config.getInstance();

    private final AuthUserDAO authUserDAO;
    private final AuthorityDAO authorityDAO;
    private final TransactionTemplate txTemplate;

    public AuthUserDBSpringClient() {
        authUserDAO = new AuthUserDAOSpringJdbc();
        authorityDAO = new AuthorityDAOSpringJdbc();
        txTemplate = new TransactionTemplate(
                new JdbcTransactionManager(
                        DataSources.dataSource(CFG.userdataJdbcUrl())));
    }

    public Optional<AuthUserEntity> findUserById(UUID id) {
        txTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        return txTemplate.execute(con -> authUserDAO.findById(id));
    }

    public Optional<AuthUserEntity> findUserByUsername(String username) {
        txTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        return txTemplate.execute(con -> authUserDAO.findByUsername(username));
    }

    public List<AuthorityEntity> findAuthorityByUserId(AuthUserEntity authUser) {
        txTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        return txTemplate.execute(con -> authorityDAO.findByAuthUserId(authUser));
    }

}

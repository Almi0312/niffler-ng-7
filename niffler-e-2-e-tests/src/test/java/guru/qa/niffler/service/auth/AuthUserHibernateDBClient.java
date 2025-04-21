package guru.qa.niffler.service.auth;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.repository.auth.AuthUserRepository;
import guru.qa.niffler.data.repository.auth.impl.AuthUserHibernateRepository;
import guru.qa.niffler.data.jdbc.DataSources;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;
import java.util.UUID;

public class AuthUserHibernateDBClient {
    private static final Config CFG = Config.getInstance();

    private final AuthUserRepository authUserRepo;

    private final TransactionTemplate txTemplate;

    public AuthUserHibernateDBClient() {
        authUserRepo = new AuthUserHibernateRepository();
        txTemplate = new TransactionTemplate(
                new JdbcTransactionManager(
                        DataSources.dataSource(CFG.userdataJdbcUrl())));
    }

    public Optional<AuthUserEntity> findUserById(UUID id) {
        txTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        return txTemplate.execute(con -> authUserRepo.findById(id));
    }

    public Optional<AuthUserEntity> findUserByUsername(String username) {
        txTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        return txTemplate.execute(con -> authUserRepo.findByUsername(username));
    }
}
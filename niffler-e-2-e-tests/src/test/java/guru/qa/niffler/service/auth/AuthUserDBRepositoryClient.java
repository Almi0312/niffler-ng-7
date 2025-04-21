package guru.qa.niffler.service.auth;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.repository.auth.AuthUserRepository;
import guru.qa.niffler.data.repository.auth.impl.AuthUserRepositoryJdbc;
import guru.qa.niffler.data.template.JdbcTransactionTemplate;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
public class AuthUserDBRepositoryClient {
    private static final Config CFG = Config.getInstance();

    private final AuthUserRepository authUserRepo;
    private final JdbcTransactionTemplate txTemplate;

    public AuthUserDBRepositoryClient() {
        authUserRepo = new AuthUserRepositoryJdbc();
        txTemplate = new JdbcTransactionTemplate(CFG.userdataJdbcUrl());
    }

    public @Nonnull Optional<AuthUserEntity> findUserById(UUID id) {
        return txTemplate.execute(
                () -> authUserRepo.findById(id)
        );
    }

    public @Nonnull Optional<AuthUserEntity> findUserByUsername(String username) {
        return txTemplate.execute(
                () -> authUserRepo.findByUsername(username)
        );
    }
}
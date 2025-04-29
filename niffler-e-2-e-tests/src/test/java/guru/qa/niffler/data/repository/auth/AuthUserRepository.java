package guru.qa.niffler.data.repository.auth;

import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.repository.auth.impl.AuthUserHibernateRepository;
import guru.qa.niffler.data.repository.auth.impl.AuthUserRepositoryJdbc;
import guru.qa.niffler.data.repository.auth.impl.AuthUserSpringRepositoryJdbc;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
public interface AuthUserRepository {
    @Nonnull
    static AuthUserRepository getInstance() {
        return switch (System.getProperty("repository.impl", "jpa")) {
            case "jpa" -> new AuthUserHibernateRepository();
            case "jdbc" -> new AuthUserRepositoryJdbc();
            case "sjdbc" -> new AuthUserSpringRepositoryJdbc();
            default -> throw new IllegalStateException("Unexpected value: " + System.getProperty("repository.impl"));
        };
    }

    @Nonnull
    AuthUserEntity create(AuthUserEntity authUserEntity);

    @Nonnull
    AuthUserEntity update(AuthUserEntity authUserEntity);

    @Nonnull
    Optional<AuthUserEntity> findById(UUID id);

    @Nonnull
    Optional<AuthUserEntity> findByUsername(String userEntity);

    void remove(AuthUserEntity authUserEntity);
}

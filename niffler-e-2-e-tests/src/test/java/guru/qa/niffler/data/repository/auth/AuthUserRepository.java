package guru.qa.niffler.data.repository.auth;

import guru.qa.niffler.data.entity.auth.AuthUserEntity;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
public interface AuthUserRepository {
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

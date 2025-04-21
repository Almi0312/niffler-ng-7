package guru.qa.niffler.data.repository.auth.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.jpa.EntityManagers;
import guru.qa.niffler.data.repository.auth.AuthUserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
public class AuthUserHibernateRepository implements AuthUserRepository {

    private static final Config CFG = Config.getInstance();

    private final EntityManager entityManager = EntityManagers.em(CFG.authJdbcUrl());

    @Override
    public @Nonnull AuthUserEntity create(AuthUserEntity authUserEntity) {
        entityManager.joinTransaction();
        entityManager.persist(authUserEntity);
        return authUserEntity;
    }

    @Override
    public @Nonnull AuthUserEntity update(AuthUserEntity authUserEntity) {
        entityManager.joinTransaction();
        entityManager.merge(authUserEntity);
        return authUserEntity;
    }

    @Override
    public @Nonnull Optional<AuthUserEntity> findById(UUID id) {
        return Optional.ofNullable(entityManager.find(AuthUserEntity.class, id));
    }

    @Override
    public @Nonnull Optional<AuthUserEntity> findByUsername(String userEntity) {
        String query = "select u from AuthUserEntity u where u.username=: username";
        try {
            return Optional.ofNullable(entityManager.createQuery(query, AuthUserEntity.class)
                    .setParameter("username", userEntity).getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public void remove(AuthUserEntity userEntity) {
        entityManager.joinTransaction();
        userEntity = entityManager.find(AuthUserEntity.class, userEntity.getId());
        entityManager.remove(userEntity);
        entityManager.flush();
    }
}

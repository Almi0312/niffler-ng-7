package guru.qa.niffler.data.repository.auth.impl.hibernate;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.jpa.EntityManagers;
import guru.qa.niffler.data.repository.auth.AuthUserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.Optional;
import java.util.UUID;

public class AuthUserRepositoryHibernate implements AuthUserRepository {

    private static final Config CFG = Config.getInstance();

    private final EntityManager entityManager = EntityManagers.em(CFG.authJdbcUrl());

    @Override
    public AuthUserEntity create(AuthUserEntity authUserEntity) {
        entityManager.joinTransaction();
        entityManager.persist(authUserEntity);
        return authUserEntity;
    }

    @Override
    public Optional<AuthUserEntity> findById(UUID id) {
        return Optional.ofNullable(entityManager.find(AuthUserEntity.class, id));
    }

    @Override
    public Optional<AuthUserEntity> findByUsername(String userEntity) {
        String hiberQuery = "select u from UserEntity u where u.username=: username";
        try {
            return Optional.ofNullable(entityManager.createQuery(hiberQuery, AuthUserEntity.class)
                    .setParameter("username", userEntity).getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteById(AuthUserEntity authUserEntity) {

    }
}

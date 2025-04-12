package guru.qa.niffler.data.repository.userdata.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.userdata.FriendshipEntity;
import guru.qa.niffler.data.entity.userdata.FriendshipStatus;
import guru.qa.niffler.data.entity.userdata.UserdataUserEntity;
import guru.qa.niffler.data.jpa.EntityManagers;
import guru.qa.niffler.data.repository.userdata.UserdataUserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserdataRepositoryHibernate implements UserdataUserRepository {

    private static final Config CFG = Config.getInstance();

    private final EntityManager entityManager = EntityManagers.em(CFG.userdataJdbcUrl());

    @Override
    public UserdataUserEntity create(UserdataUserEntity userEntity) {
        entityManager.joinTransaction();
        entityManager.persist(userEntity);
        return userEntity;
    }

    @Override
    public Optional<UserdataUserEntity> findById(UUID id) {
        return Optional.ofNullable(entityManager.find(UserdataUserEntity.class, id));
    }

    @Override
    public Optional<UserdataUserEntity> findByUsername(String userEntity) {
        String hiberQuery = "select u from UserdataUserEntity u where u.username=: username";
        try {
            return Optional.ofNullable(entityManager.createQuery(hiberQuery, UserdataUserEntity.class)
                    .setParameter("username", userEntity).getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<UserdataUserEntity> findByIdWithFriendship(UUID id) {
        return Optional.empty();
    }

    @Override
    public Optional<UserdataUserEntity> findByUsernameWithFriendship(String username) {
        return Optional.empty();
    }

    @Override
    public List<FriendshipEntity> findUserFriendships(UserdataUserEntity user, boolean isRequester) {
        return List.of();
    }

    @Override
    public UserdataUserEntity update(UserdataUserEntity user) {
        return null;
    }

    @Override
    public void delete(UserdataUserEntity userdataUserEntity) {

    }

    @Override
    public void createOutcomeInvitations(FriendshipStatus status, UserdataUserEntity addressee, UserdataUserEntity... requester) {
        entityManager.joinTransaction();
        addressee.addFriends(status, requester);
    }
}

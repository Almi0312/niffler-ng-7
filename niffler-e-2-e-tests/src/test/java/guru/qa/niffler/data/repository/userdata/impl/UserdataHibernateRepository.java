package guru.qa.niffler.data.repository.userdata.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.userdata.FriendshipEntity;
import guru.qa.niffler.data.entity.userdata.FriendshipStatus;
import guru.qa.niffler.data.entity.userdata.UserdataUserEntity;
import guru.qa.niffler.data.jpa.EntityManagers;
import guru.qa.niffler.data.repository.userdata.UserdataRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.*;

public class UserdataHibernateRepository implements UserdataRepository {

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
        String query = "select u from UserdataUserEntity u where u.username=: username";
        try {
            return Optional.ofNullable(entityManager.createQuery(query, UserdataUserEntity.class)
                    .setParameter("username", userEntity).getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<UserdataUserEntity> findByIdWithFriendship(UUID id) {
        return findById(id);
    }

    @Override
    public Optional<UserdataUserEntity> findByUsernameWithFriendship(String username) {
        return findByUsername(username);
    }

    @Override
    public List<FriendshipEntity> findUserFriendships(UserdataUserEntity user, boolean isRequester) {
        throw new RuntimeException("Метод не актуален из за fetch = Lazy для коллекций внутри UserdataUserEntity");
    }

    @Override
    public UserdataUserEntity update(UserdataUserEntity user) {
        entityManager.joinTransaction();
        entityManager.merge(user);
        return user;
    }

    @Override
    public void remove(UserdataUserEntity user) {
        entityManager.joinTransaction();
        user = entityManager.find(UserdataUserEntity.class, user.getId());
        entityManager.remove(user);
        entityManager.flush();
    }

    @Override
    public void sendInvitation(FriendshipStatus status, UserdataUserEntity outcome, UserdataUserEntity... incomes) {
        entityManager.joinTransaction();
        outcome.addFriends(status, incomes);
    }

    @Override
    public void addFriend(UserdataUserEntity outcome, UserdataUserEntity... incomes) {
        entityManager.joinTransaction();
        outcome.addFriends(FriendshipStatus.ACCEPTED, incomes);
        Arrays.stream(incomes).forEach(income ->
                income.addFriends(FriendshipStatus.ACCEPTED, outcome));
    }
}

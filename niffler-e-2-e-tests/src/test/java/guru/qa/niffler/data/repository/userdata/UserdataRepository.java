package guru.qa.niffler.data.repository.userdata;

import guru.qa.niffler.data.entity.userdata.FriendshipEntity;
import guru.qa.niffler.data.entity.userdata.FriendshipStatus;
import guru.qa.niffler.data.entity.userdata.UserdataUserEntity;
import guru.qa.niffler.data.repository.userdata.impl.UserdataHibernateRepository;
import guru.qa.niffler.data.repository.userdata.impl.UserdataRepositoryJdbc;
import guru.qa.niffler.data.repository.userdata.impl.UserdataSpringRepositoryJdbc;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
public interface UserdataRepository {
    @Nonnull
    static UserdataRepository getInstance() {
        return switch (System.getProperty("repository.impl", "jpa")) {
            case "jpa" -> new UserdataHibernateRepository();
            case "jdbc" -> new UserdataRepositoryJdbc();
            case "sjdbc" -> new UserdataSpringRepositoryJdbc();
            default -> throw new IllegalStateException("Unexpected value: " + System.getProperty("repository.impl"));
        };
    }

    @Nonnull
    UserdataUserEntity create(UserdataUserEntity userdataUserEntity);

    @Nonnull
    Optional<UserdataUserEntity> findById(UUID id);

    @Nonnull
    Optional<UserdataUserEntity> findByIdWithFriendship(UUID id);

    @Nonnull
    Optional<UserdataUserEntity> findByUsername(String username);

    @Nonnull
    Optional<UserdataUserEntity> findByUsernameWithFriendship(String username);

    @Nonnull
    List<FriendshipEntity> findUserFriendships(UserdataUserEntity user, boolean isRequester);

    @Nonnull
    UserdataUserEntity update(UserdataUserEntity user);

    void remove(UserdataUserEntity userdataUserEntity);

    void sendInvitation(FriendshipStatus status, UserdataUserEntity requester, UserdataUserEntity... addressees);

    void addFriend(UserdataUserEntity outcome, UserdataUserEntity... incomes);
}

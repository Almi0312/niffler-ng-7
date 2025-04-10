package guru.qa.niffler.data.repository.userdata;

import guru.qa.niffler.data.entity.userdata.FriendshipEntity;
import guru.qa.niffler.data.entity.userdata.FriendshipStatus;
import guru.qa.niffler.data.entity.userdata.UserdataUserEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserdataUserRepository {
    UserdataUserEntity create(UserdataUserEntity userdataUserEntity);

    Optional<UserdataUserEntity> findById(UUID id);

    Optional<UserdataUserEntity> findByIdWithFriendship(UUID id);

    Optional<UserdataUserEntity> findByUsername(String username);

    Optional<UserdataUserEntity> findByUsernameWithFriendship(String username);

    List<FriendshipEntity> findUserFriendships(UserdataUserEntity user, boolean isRequester);

    UserdataUserEntity update(UserdataUserEntity user);

    void delete(UserdataUserEntity userdataUserEntity);

    void createRequester(FriendshipStatus status, UserdataUserEntity requester, UserdataUserEntity... addressees);

    void createAddressee(UserdataUserEntity addressee, UserdataUserEntity... requesters);

}

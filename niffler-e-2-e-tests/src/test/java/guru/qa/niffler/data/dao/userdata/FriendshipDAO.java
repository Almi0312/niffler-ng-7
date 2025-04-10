package guru.qa.niffler.data.dao.userdata;

import guru.qa.niffler.data.entity.userdata.FriendshipEntity;
import guru.qa.niffler.data.entity.userdata.UserdataUserEntity;

import java.util.List;

public interface FriendshipDAO {
    void create(List<FriendshipEntity> friends);
    List<FriendshipEntity> findUserFriendships(UserdataUserEntity user, boolean isRequester);
    void delete(UserdataUserEntity userEntity);
}

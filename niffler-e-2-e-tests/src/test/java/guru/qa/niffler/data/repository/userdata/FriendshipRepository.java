package guru.qa.niffler.data.repository.userdata;

import guru.qa.niffler.data.entity.userdata.UserdataUserEntity;

public interface FriendshipRepository {
    void create(UserdataUserEntity requester, UserdataUserEntity addressee);

}

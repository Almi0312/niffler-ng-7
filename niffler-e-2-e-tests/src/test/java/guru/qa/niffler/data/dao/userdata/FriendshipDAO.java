package guru.qa.niffler.data.dao.userdata;

import guru.qa.niffler.data.entity.userdata.FriendshipEntity;
import guru.qa.niffler.data.entity.userdata.UserdataUserEntity;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public interface FriendshipDAO {
    void create(List<FriendshipEntity> friends);

    @Nonnull
    List<FriendshipEntity> findUserFriendships(UserdataUserEntity user, boolean isRequester);

    void delete(UserdataUserEntity userEntity);
}

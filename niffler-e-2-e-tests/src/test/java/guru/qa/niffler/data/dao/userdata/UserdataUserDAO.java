package guru.qa.niffler.data.dao.userdata;

import guru.qa.niffler.data.entity.userdata.UserdataUserEntity;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
public interface UserdataUserDAO {
    @Nonnull
    UserdataUserEntity create(UserdataUserEntity userdataUserEntity);

    @Nonnull
    UserdataUserEntity update(UserdataUserEntity user);

    @Nonnull
    Optional<UserdataUserEntity> findById(UUID id);

    @Nonnull
    Optional<UserdataUserEntity> findByUsername(String username);

    @Nonnull
    List<UserdataUserEntity> findAll();

    void delete(UserdataUserEntity userdataUserEntity);

}

package guru.qa.niffler.data.dao.userdata;

import guru.qa.niffler.data.entity.userdata.UserdataUserEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserdataUserDAO {
    UserdataUserEntity create(UserdataUserEntity userdataUserEntity);
    UserdataUserEntity update(UserdataUserEntity user);
    Optional<UserdataUserEntity> findById(UUID id);
    Optional<UserdataUserEntity> findByUsername(String username);
    List<UserdataUserEntity> findAll();
    void delete(UserdataUserEntity userdataUserEntity);

}

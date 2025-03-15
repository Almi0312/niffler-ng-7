package guru.qa.niffler.data.dao.auth;

import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;

import java.util.List;

public interface AuthorityDAO {
    void create(AuthorityEntity... authority);
    List<AuthorityEntity> findByAuthUserId(AuthUserEntity authUser);
    void delete(AuthUserEntity authUser);
}

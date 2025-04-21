package guru.qa.niffler.data.repository.auth.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.auth.AuthUserDAO;
import guru.qa.niffler.data.dao.auth.AuthorityDAO;
import guru.qa.niffler.data.dao.auth.impl.default_jdbc.AuthUserDAOJdbc;
import guru.qa.niffler.data.dao.auth.impl.default_jdbc.AuthorityDAOJdbc;
import guru.qa.niffler.data.dao.auth.mapper.AuthUserEntityRowMapper;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.Authority;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import guru.qa.niffler.data.repository.auth.AuthUserRepository;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static guru.qa.niffler.data.jdbc.Connections.holder;

@ParametersAreNonnullByDefault
public class AuthUserRepositoryJdbc implements AuthUserRepository {

    private static final Config CFG = Config.getInstance();

    AuthUserDAO authUserDAO = new AuthUserDAOJdbc();
    AuthorityDAO authorityDAO = new AuthorityDAOJdbc();

    @Override
    public @Nonnull AuthUserEntity create(AuthUserEntity authUserEntity) {
        authUserEntity.setId(authUserDAO.create(authUserEntity).getId());
        authorityDAO.create(authUserEntity.getAuthorities().toArray(new AuthorityEntity[0]));
        return authUserEntity;
    }

    @Override
    public @Nonnull AuthUserEntity update(AuthUserEntity authUserEntity) {
        return authUserDAO.update(authUserEntity);
    }

    @SuppressWarnings("resource")
    @Override
    public @Nonnull Optional<AuthUserEntity> findById(UUID id) {
        String query = "SELECT * FROM \"user\" u JOIN authority a ON (u.id = a.user_id) WHERE u.id = ?";
        try (PreparedStatement ps = holder(CFG.authJdbcUrl()).connection()
                .prepareStatement(
                        query)) {
            ps.setObject(1, id);
            ps.execute();
            try (ResultSet resultSet = ps.getResultSet()) {
                AuthUserEntity user = null;
                List<AuthorityEntity> authorityEntities = new ArrayList<>();
                while (resultSet.next()) {
                    if (user == null) {
                        user = AuthUserEntityRowMapper.instance.mapRow(resultSet, 1);
                    }
                    AuthorityEntity authorityEntity = new AuthorityEntity();
                    authorityEntity.setId(resultSet.getObject("a.id", UUID.class));
                    authorityEntity.setUser(user);
                    authorityEntity.setAuthority(Authority.valueOf(resultSet.getString("authority")));
                    authorityEntities.add(authorityEntity);
                }
                if (user == null) {
                    return Optional.empty();
                } else {
                    user.setAuthorities(authorityEntities);
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("resource")
    @Override
    public @Nonnull Optional<AuthUserEntity> findByUsername(String username) {
        String query = "SELECT u.id, u.username, u.password, u.enabled, u.account_non_expired," +
                " u.account_non_locked, u.credentials_non_expired, a.id AS authority_id, a.user_id, a.authority" +
                " FROM \"user\" AS u JOIN authority AS a ON(u.id = a.user_id) WHERE u.username = ?";
        try (PreparedStatement preparedStatement = holder(CFG.authJdbcUrl()).connection()
                .prepareStatement(query)) {
            preparedStatement.setString(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                AuthUserEntity user = null;
                List<AuthorityEntity> authorityEntities = new ArrayList<>();
                while (resultSet.next()) {
                    if (user == null) {
                        user = AuthUserEntityRowMapper.instance.mapRow(resultSet, 1);
                    }
                    AuthorityEntity authorityEntity = new AuthorityEntity();
                    authorityEntity.setId(resultSet.getObject("authority_id", UUID.class));
                    authorityEntity.setUser(user);
                    authorityEntity.setAuthority(Authority.valueOf(resultSet.getString("authority")));
                    authorityEntities.add(authorityEntity);
                }
                if (user == null) {
                    return Optional.empty();
                } else {
                    user.setAuthorities(authorityEntities);
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove(AuthUserEntity authUserEntity) {
        authorityDAO.delete(authUserEntity);
        authUserDAO.delete(authUserEntity);
    }
}
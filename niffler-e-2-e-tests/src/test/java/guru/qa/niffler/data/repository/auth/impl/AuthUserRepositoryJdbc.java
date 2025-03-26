package guru.qa.niffler.data.repository.auth.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.auth.AuthUserDAO;
import guru.qa.niffler.data.dao.auth.mapper.AuthUserEntityRowMapper;
import guru.qa.niffler.data.dao.auth.mapper.AuthorityEntityRowMapper;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.Authority;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import guru.qa.niffler.data.repository.auth.AuthUserRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static guru.qa.niffler.data.template.Connections.holder;

public class AuthUserRepositoryJdbc implements AuthUserRepository {

    private static final Config CFG = Config.getInstance();

    @Override
    public AuthUserEntity create(AuthUserEntity authUserEntity) {
        String userCreateQuery = "INSERT INTO \"user\" (username, password, enabled, account_non_expired, " +
                "account_non_locked, credentials_non_expired) VALUES (?, ?, ?, ?, ?, ?)";
        String authorityCreateQuery = "INSERT INTO \"authority\" (user_id, authority) VALUES (?, ?)";
        try (PreparedStatement userCreatePs = holder(CFG.authJdbcUrl()).connection()
                .prepareStatement(userCreateQuery, PreparedStatement.RETURN_GENERATED_KEYS);
             PreparedStatement authorityCreatePs = holder(CFG.authJdbcUrl()).connection()
                     .prepareStatement(authorityCreateQuery)) {
            userCreatePs.setString(1, authUserEntity.getUsername());
            userCreatePs.setObject(2, authUserEntity.getPassword());
            userCreatePs.setBoolean(3, authUserEntity.getEnabled());
            userCreatePs.setBoolean(4, authUserEntity.getAccountNonExpired());
            userCreatePs.setBoolean(5, authUserEntity.getAccountNonLocked());
            userCreatePs.setBoolean(6, authUserEntity.getCredentialsNonExpired());
            userCreatePs.executeUpdate();
            final UUID generatedKey;
            try (ResultSet resultSet = userCreatePs.getGeneratedKeys()) {
                if (resultSet.next()) {
                    generatedKey = resultSet.getObject("id", UUID.class);
                } else {
                    throw new SQLException("Can't find user_id in ResultSet");
                }
                authUserEntity.setId(generatedKey);
                for (AuthorityEntity a : authUserEntity.getAuthorities()) {
                    authorityCreatePs.setObject(1, generatedKey);
                    authorityCreatePs.setString(2, a.getAuthority().name());
                    authorityCreatePs.addBatch();
                    authorityCreatePs.clearParameters();
                }
                authorityCreatePs.executeBatch();
                return authUserEntity;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<AuthUserEntity> findById(UUID id) {
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

    @Override
    public Optional<AuthUserEntity> findByUsername(String username) {
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
                    authorityEntity.setId(resultSet.getObject("authID", UUID.class));
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
    public void deleteById(AuthUserEntity authUserEntity) {
        String deleteUserQuery = "DELETE FROM \"user\" WHERE username = ?";
        String deleteAuthorityQuery = "DELETE FROM authority WHERE user_id = ?";
        try (PreparedStatement userPs = holder(CFG.authJdbcUrl()).connection()
                .prepareStatement(deleteUserQuery);
             PreparedStatement authorityPs = holder(CFG.authJdbcUrl()).connection()
                     .prepareStatement(deleteAuthorityQuery)
        ) {
            authorityPs.setObject(1, authUserEntity.getId());
            authorityPs.executeUpdate();

            userPs.setString(1, authUserEntity.getUsername());
            userPs.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
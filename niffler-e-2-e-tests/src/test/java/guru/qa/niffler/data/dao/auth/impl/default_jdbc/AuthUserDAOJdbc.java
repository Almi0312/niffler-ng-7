package guru.qa.niffler.data.dao.auth.impl.default_jdbc;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.auth.AuthUserDAO;
import guru.qa.niffler.data.dao.auth.mapper.AuthUserEntityRowMapper;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import static guru.qa.niffler.data.template.Connections.holder;

public class AuthUserDAOJdbc implements AuthUserDAO {

    private static final Config CFG = Config.getInstance();

    private final String authUrlJdbc = CFG.authJdbcUrl();

    @Override
    public AuthUserEntity create(AuthUserEntity authUserEntity) {
        String query = "INSERT INTO \"user\" (username, password, enabled, account_non_expired, " +
                "account_non_locked, credentials_non_expired) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = holder(authUrlJdbc).connection()
                .prepareStatement(
                query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, authUserEntity.getUsername());
            preparedStatement.setObject(2, authUserEntity.getPassword());
            preparedStatement.setBoolean(3, authUserEntity.getEnabled());
            preparedStatement.setBoolean(4, authUserEntity.getAccountNonExpired());
            preparedStatement.setBoolean(5, authUserEntity.getAccountNonLocked());
            preparedStatement.setBoolean(6, authUserEntity.getCredentialsNonExpired());
            preparedStatement.executeUpdate();
            final UUID generatedKey;
            try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    generatedKey = resultSet.getObject("id", UUID.class);
                } else {
                    throw new SQLException("Can't find user_id in ResultSet");
                }
                authUserEntity.setId(generatedKey);
                return authUserEntity;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AuthUserEntity update(AuthUserEntity authUserEntity) {
        String userUpdateQuery = """
                UPDATE \"user\" SET 
                username = ?, 
                password = ?, 
                enabled = ?, 
                account_non_expired = ?,
                "account_non_locked = ?, 
                credentials_non_expired = ?
                WHERE id = ?"
                """;
        try (PreparedStatement userUpdatePs = holder(CFG.authJdbcUrl()).connection()
                .prepareStatement(userUpdateQuery)) {
            userUpdatePs.setString(1, authUserEntity.getUsername());
            userUpdatePs.setObject(2, authUserEntity.getPassword());
            userUpdatePs.setBoolean(3, authUserEntity.getEnabled());
            userUpdatePs.setBoolean(4, authUserEntity.getAccountNonExpired());
            userUpdatePs.setBoolean(5, authUserEntity.getAccountNonLocked());
            userUpdatePs.setBoolean(6, authUserEntity.getCredentialsNonExpired());
            userUpdatePs.executeUpdate();
            return authUserEntity;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<AuthUserEntity> findById(UUID id) {
        String query = "SELECT * FROM \"user\" WHERE id = ?";
        try (PreparedStatement ps = holder(authUrlJdbc).connection()
                .prepareStatement(
                        query)) {
            ps.setObject(1, id);

            ps.execute();

            try (ResultSet rs = ps.getResultSet()) {
                if (rs.next()) {
                    return Optional.of(AuthUserEntityRowMapper.instance.mapRow(rs, 0));
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<AuthUserEntity> findByUsername(String username) {
        String query = "SELECT * FROM \"user\" WHERE username = ?";
        try (PreparedStatement preparedStatement = holder(authUrlJdbc).connection()
                .prepareStatement(
                        query)) {
            preparedStatement.setString(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(AuthUserEntityRowMapper.instance.mapRow(resultSet, 0));
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(AuthUserEntity authUserEntity) {
        String query = "DELETE FROM \"user\" WHERE id = ?";
        try (PreparedStatement preparedStatement = holder(authUrlJdbc).connection()
                .prepareStatement(
                        query)) {
            preparedStatement.setObject(1, authUserEntity.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
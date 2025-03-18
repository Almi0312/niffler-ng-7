package guru.qa.niffler.data.dao.auth.impl.default_jdbc;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.auth.AuthUserDAO;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import static guru.qa.niffler.data.template.Connections.holder;

public class AuthUserDAOJdbc implements AuthUserDAO {

    private static final Config CFG = Config.getInstance();

    @Override
    public AuthUserEntity create(AuthUserEntity authUserEntity) {
        String query = "INSERT INTO \"user\" (username, password, enabled, account_non_expired, " +
                "account_non_locked, credentials_non_expired) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = holder(CFG.authJdbcUrl()).connection()
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
    public Optional<AuthUserEntity> findById(UUID id) {
        String query = "SELECT * FROM \"user\" WHERE id = ?";
        try (PreparedStatement ps = holder(CFG.authJdbcUrl()).connection()
                .prepareStatement(
                        query)) {
            ps.setObject(1, id);

            ps.execute();

            try (ResultSet rs = ps.getResultSet()) {
                if (rs.next()) {
                    AuthUserEntity result = new AuthUserEntity();
                    result.setId(rs.getObject("id", UUID.class));
                    result.setUsername(rs.getString("username"));
                    result.setPassword(rs.getString("password"));
                    result.setEnabled(rs.getBoolean("enabled"));
                    result.setAccountNonExpired(rs.getBoolean("account_non_expired"));
                    result.setAccountNonLocked(rs.getBoolean("account_non_locked"));
                    result.setCredentialsNonExpired(rs.getBoolean("credentials_non_expired"));
                    return Optional.of(result);
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
        try (PreparedStatement preparedStatement = holder(CFG.authJdbcUrl()).connection()
                .prepareStatement(
                        query)) {
            preparedStatement.setString(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(extractAuthUserEntityFromResultSet(resultSet));
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteByUsername(AuthUserEntity authUserEntity) {
        String query = "DELETE FROM \"user\" WHERE username = ?";
        try (PreparedStatement preparedStatement = holder(CFG.authJdbcUrl()).connection()
                .prepareStatement(
                        query)) {
            preparedStatement.setString(1, authUserEntity.getUsername());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private AuthUserEntity extractAuthUserEntityFromResultSet(ResultSet resultSet) throws SQLException {
        AuthUserEntity authUserEntity = new AuthUserEntity();
        authUserEntity.setId(resultSet.getObject("id", UUID.class));
        authUserEntity.setUsername(resultSet.getString("username"));
        authUserEntity.setPassword(resultSet.getString("password"));
        authUserEntity.setEnabled(resultSet.getBoolean("enabled"));
        authUserEntity.setAccountNonExpired(resultSet.getBoolean("account_non_expired"));
        authUserEntity.setAccountNonLocked(resultSet.getBoolean("account_non_locked"));
        authUserEntity.setCredentialsNonExpired(resultSet.getBoolean("credentials_non_expired"));
        return authUserEntity;
    }
}
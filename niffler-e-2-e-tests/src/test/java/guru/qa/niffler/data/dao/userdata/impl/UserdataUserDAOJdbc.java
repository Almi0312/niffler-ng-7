package guru.qa.niffler.data.dao.userdata.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.userdata.UserdataUserDAO;
import guru.qa.niffler.data.entity.userdata.UserdataUserEntity;
import guru.qa.niffler.model.CurrencyValues;

import java.sql.*;
import java.util.Optional;
import java.util.UUID;

import static guru.qa.niffler.data.template.Connections.holder;

public class UserdataUserDAOJdbc implements UserdataUserDAO {

    private static final Config CFG = Config.getInstance();

    @Override
    public UserdataUserEntity create(UserdataUserEntity userdataUserEntity) {
        String query = "INSERT INTO \"user\" (username, currency) VALUES (?, ?)";
        try (PreparedStatement preparedStatement = holder(CFG.userdataJdbcUrl()).connection()
                .prepareStatement(
                        query, Statement.RETURN_GENERATED_KEYS
                )) {
            preparedStatement.setString(1, userdataUserEntity.getUsername());
            preparedStatement.setString(2, userdataUserEntity.getCurrency().name());

            preparedStatement.executeUpdate();
            final UUID generatedKey;
            try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    generatedKey = resultSet.getObject("id", UUID.class);
                } else {
                    throw new SQLException("Can't find id in ResultSet");
                }
                userdataUserEntity.setId(generatedKey);
                return userdataUserEntity;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<UserdataUserEntity> findById(UUID id) {
        String query = "SELECT * FROM \"user\" WHERE id = ?";
        try (PreparedStatement preparedStatement = holder(CFG.userdataJdbcUrl()).connection()
                .prepareStatement(
                        query)) {
            preparedStatement.setObject(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    UserdataUserEntity userdataUserEntity = extractUserEntityFromResultSet(resultSet);
                    return Optional.of(userdataUserEntity);
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<UserdataUserEntity> findByUsername(String username) {
        String query = "SELECT * FROM \"user\" WHERE username = ?";
        try (PreparedStatement preparedStatement = holder(CFG.userdataJdbcUrl()).connection()
                .prepareStatement(
                        query)) {
            preparedStatement.setObject(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(extractUserEntityFromResultSet(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(UserdataUserEntity userdataUserEntity) {
        String query = "DELETE FROM \"user\" WHERE id = ?";
        try (PreparedStatement preparedStatement = holder(CFG.userdataJdbcUrl()).connection()
                .prepareStatement(
                        query)) {
            preparedStatement.setObject(1, userdataUserEntity.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private UserdataUserEntity extractUserEntityFromResultSet(ResultSet resultSet) throws SQLException {
        UserdataUserEntity userdataUserEntity = new UserdataUserEntity();
        userdataUserEntity.setId(resultSet.getObject("id", UUID.class));
        userdataUserEntity.setUsername(resultSet.getString("username"));
        userdataUserEntity.setCurrency(CurrencyValues.valueOf(resultSet.getString("currency")));
        userdataUserEntity.setFirstname(resultSet.getString("firstname"));
        userdataUserEntity.setSurname(resultSet.getString("surname"));
        userdataUserEntity.setPhoto(resultSet.getBytes("photo"));
        userdataUserEntity.setPhotoSmall(resultSet.getBytes("photo_small"));
        userdataUserEntity.setFullname(resultSet.getString("full_name"));
        return userdataUserEntity;
    }
}

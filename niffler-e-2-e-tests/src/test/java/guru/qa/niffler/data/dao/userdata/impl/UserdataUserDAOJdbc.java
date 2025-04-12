package guru.qa.niffler.data.dao.userdata.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.userdata.UserdataUserDAO;
import guru.qa.niffler.data.dao.userdata.mapper.UdUserEntityRowMapper;
import guru.qa.niffler.data.entity.userdata.UserdataUserEntity;

import java.sql.*;
import java.util.*;

import static guru.qa.niffler.data.template.Connections.holder;

public class UserdataUserDAOJdbc implements UserdataUserDAO {

    private static final Config CFG = Config.getInstance();
    private final String udUrlJdbc = CFG.userdataJdbcUrl();

    @Override
    public UserdataUserEntity create(UserdataUserEntity user) {
        String query = "INSERT INTO \"user\" (username, currency, firstname, surname, photo, photo_small, full_name) " +
                "VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = holder(udUrlJdbc).connection()
                .prepareStatement(
                        query, Statement.RETURN_GENERATED_KEYS
                )) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getCurrency().name());
            ps.setString(3, user.getFirstname());
            ps.setString(4, user.getSurname());
            ps.setBytes(5, user.getPhoto());
            ps.setBytes(6, user.getPhotoSmall());
            ps.setString(7, user.getFullname());
            ps.executeUpdate();
            final UUID generatedKey;
            try (ResultSet resultSet = ps.getGeneratedKeys()) {
                if (resultSet.next()) {
                    generatedKey = resultSet.getObject("id", UUID.class);
                } else {
                    throw new SQLException("Can't find id in ResultSet");
                }
                user.setId(generatedKey);
                return user;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public UserdataUserEntity update(UserdataUserEntity user) {
        String queryUpdateUser = "UPDATE \"user\" SET username = ?, currency = ?, firstname = ?, surname = ?, photo = ?, " +
                "photo_small = ?, full_name = ? WHERE id = ?";
        try (PreparedStatement updateUserPs = holder(udUrlJdbc).connection().prepareStatement(queryUpdateUser)) {
            updateUserPs.setString(1, user.getUsername());
            updateUserPs.setString(2, user.getCurrency().name());
            updateUserPs.setString(3, user.getFirstname());
            updateUserPs.setString(4, user.getSurname());
            updateUserPs.setBytes(5, user.getPhoto());
            updateUserPs.setBytes(6, user.getPhotoSmall());
            updateUserPs.setString(7, user.getFullname());
            updateUserPs.setObject(8, user.getId());
            updateUserPs.executeUpdate();
            return user;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<UserdataUserEntity> findById(UUID id) {
        String query = "SELECT * FROM \"user\" WHERE id = ?";
        try (PreparedStatement preparedStatement = holder(udUrlJdbc).connection()
                .prepareStatement(
                        query)) {
            preparedStatement.setObject(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(UdUserEntityRowMapper.instance.mapRow(resultSet, 0));
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
        try (PreparedStatement preparedStatement = holder(udUrlJdbc).connection()
                .prepareStatement(
                        query)) {
            preparedStatement.setObject(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(UdUserEntityRowMapper.instance.mapRow(resultSet, 0));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<UserdataUserEntity> findAll() {
        String query = "SELECT * FROM \"user\" u";
        try (PreparedStatement preparedStatement = holder(udUrlJdbc).connection()
                .prepareStatement(
                        query)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<UserdataUserEntity> userEntities = new ArrayList<>();
                while (resultSet.next()) {
                    userEntities.add(UdUserEntityRowMapper.instance.mapRow(resultSet, 0));
                }
                return userEntities;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(UserdataUserEntity userdataUserEntity) {
        String query = "DELETE FROM \"user\" WHERE id = ?";
        try (PreparedStatement preparedStatement = holder(udUrlJdbc).connection()
                .prepareStatement(
                        query)) {
            preparedStatement.setObject(1, userdataUserEntity.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

package guru.qa.niffler.data.dao.spend.impl.default_jdbc;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.spend.CategoryDAO;
import guru.qa.niffler.data.dao.spend.mapper.CategoryEntityRowMapper;
import guru.qa.niffler.data.entity.spend.CategoryEntity;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static guru.qa.niffler.data.jdbc.Connections.holder;

@ParametersAreNonnullByDefault
public class CategoryDAOJdbc implements CategoryDAO {

    private static final Config CFG = Config.getInstance();

    private final String spdUrlJdbc = CFG.spendJdbcUrl();

    public CategoryDAOJdbc() {
    }

    @SuppressWarnings("resource")
    @Nonnull
    @Override
    public CategoryEntity create(CategoryEntity category) {
        String query = "INSERT INTO category (username, name, archived) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = holder(spdUrlJdbc).connection()
                .prepareStatement(
                        query, Statement.RETURN_GENERATED_KEYS
                )) {
            preparedStatement.setString(1, category.getUsername());
            preparedStatement.setString(2, category.getName());
            preparedStatement.setBoolean(3, category.isArchived());
            preparedStatement.executeUpdate();
            final UUID generatedKey;
            try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    generatedKey = resultSet.getObject("id", UUID.class);
                } else {
                    throw new SQLException("Can't find id in ResultSet");
                }
            }
            category.setId(generatedKey);
            return category;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("resource")
    @Nonnull
    @Override
    public Optional<CategoryEntity> findById(UUID id) {
        String query = "SELECT * FROM category WHERE id = ?";
        try (PreparedStatement preparedStatement = holder(spdUrlJdbc).connection()
                .prepareStatement(
                        query)) {
            preparedStatement.setObject(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(CategoryEntityRowMapper.instance.mapRow(resultSet, 0));
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("resource")
    @Nonnull
    @Override
    public Optional<CategoryEntity> findByUsernameAndName(String username, String categoryName) {
        String query = "SELECT * FROM category WHERE username = ? and name = ?";
        try (PreparedStatement preparedStatement = holder(spdUrlJdbc).connection()
                .prepareStatement(
                        query)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, categoryName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(CategoryEntityRowMapper.instance.mapRow(resultSet, 0));
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("resource")
    @Nonnull
    @Override
    public List<CategoryEntity> findAllByUsername(String username) {
        String query = "SELECT * FROM category WHERE username = ?";
        try (PreparedStatement preparedStatement = holder(spdUrlJdbc).connection()
                .prepareStatement(
                        query)) {
            preparedStatement.setString(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<CategoryEntity> categoryEntities = new ArrayList<>();
                while (resultSet.next()) {
                    categoryEntities.add(CategoryEntityRowMapper.instance.mapRow(resultSet, 0));
                }
                return categoryEntities;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("resource")
    @Nonnull
    @Override
    public List<CategoryEntity> findAll() {
        String query = "SELECT * FROM category";
        try (PreparedStatement preparedStatement = holder(spdUrlJdbc).connection()
                .prepareStatement(
                        query)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<CategoryEntity> entities = new ArrayList<>();
                while (resultSet.next()) {
                    entities.add(CategoryEntityRowMapper.instance.mapRow(resultSet, 0));
                }
                return entities;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("resource")
    @Nonnull
    @Override
    public CategoryEntity update(CategoryEntity category) {
        String query = "UPDATE category SET username = ?, name = ?, archived = ? " +
                "WHERE id = ?";
        try (PreparedStatement preparedStatement = holder(spdUrlJdbc).connection()
                .prepareStatement(
                        query, Statement.RETURN_GENERATED_KEYS
                )) {
            preparedStatement.setString(1, category.getUsername());
            preparedStatement.setString(2, category.getName());
            preparedStatement.setBoolean(3, category.isArchived());
            preparedStatement.setObject(4, category.getId());
            preparedStatement.executeUpdate();
            return category;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("resource")
    @Override
    public void delete(CategoryEntity category) {
        String query = "DELETE FROM category WHERE id = ?";
        try (PreparedStatement preparedStatement = holder(spdUrlJdbc).connection()
                .prepareStatement(
                        query)) {
            preparedStatement.setObject(1, category.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
package guru.qa.niffler.data.dao.spend.impl.default_jdbc;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.spend.SpendDAO;
import guru.qa.niffler.data.dao.spend.mapper.SpendEntityRowMapper;
import guru.qa.niffler.data.entity.spend.SpendEntity;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static guru.qa.niffler.data.jdbc.Connections.holder;

@ParametersAreNonnullByDefault
public class SpendDAOJdbc implements SpendDAO {

    private static final Config CFG = Config.getInstance();

    private final String spdUrlJdbc = CFG.spendJdbcUrl();

    public SpendDAOJdbc() {
    }

    @SuppressWarnings("resource")
    @Nonnull
    @Override
    public SpendEntity create(SpendEntity spend) {
        String query = "INSERT INTO spend (username, spend_date, currency, amount, description, category_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = holder(spdUrlJdbc).connection()
                .prepareStatement(
                        query, Statement.RETURN_GENERATED_KEYS
                )) {
            preparedStatement.setString(1, spend.getUsername());
            preparedStatement.setDate(2, new Date(spend.getSpendDate().getTime()));
            preparedStatement.setString(3, spend.getCurrency().name());
            preparedStatement.setDouble(4, spend.getAmount());
            preparedStatement.setString(5, spend.getDescription());
            preparedStatement.setObject(6, spend.getCategory().getId());
            preparedStatement.executeUpdate();
            final UUID generatedKey;
            try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    generatedKey = resultSet.getObject("id", UUID.class);
                } else {
                    throw new SQLException("Can't find id in ResultSet");
                }
            }
            spend.setId(generatedKey);
            return spend;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("resource")
    @Nonnull
    @Override
    public SpendEntity update(SpendEntity spend) {
        String query = "UPDATE spend SET" +
                " spend_date = ?, currency = ?, amount = ?, description = ?" +
                " WHERE id = ?";
        try (PreparedStatement ps = holder(spdUrlJdbc).connection().prepareStatement(
                query);
        ) {
            ps.setDate(1, new java.sql.Date(spend.getSpendDate().getTime()));
            ps.setString(2, spend.getCurrency().name());
            ps.setDouble(3, spend.getAmount());
            ps.setString(4, spend.getDescription());
            ps.setObject(5, spend.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return spend;
    }

    @SuppressWarnings("resource")
    @Nonnull
    @Override
    public Optional<SpendEntity> findById(UUID id) {
        String query = "SELECT * FROM spend WHERE id = ?";
        try (PreparedStatement preparedStatement = holder(spdUrlJdbc).connection()
                .prepareStatement(
                        query)) {
            preparedStatement.setObject(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(SpendEntityRowMapper.instance.mapRow(resultSet, 0));
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
    public List<SpendEntity> findAllByUsername(String username) {
        String query = "SELECT * FROM spend WHERE username = ?";
        try (PreparedStatement preparedStatement = holder(spdUrlJdbc).connection()
                .prepareStatement(
                        query)) {
            preparedStatement.setString(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<SpendEntity> spendEntities = new ArrayList<>();
                while (resultSet.next()) {
                    spendEntities.add(SpendEntityRowMapper.instance.mapRow(resultSet, 0));
                }
                return spendEntities;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("resource")
    @Nonnull
    @Override
    public List<SpendEntity> findAll() {
        String query = "SELECT * FROM spend";
        try (PreparedStatement ps = holder(spdUrlJdbc).connection()
                .prepareStatement(
                        query)) {
            try (ResultSet resultSet = ps.executeQuery()) {
                List<SpendEntity> entities = new ArrayList<>();
                while (resultSet.next()) {
                    entities.add(SpendEntityRowMapper.instance.mapRow(resultSet, 0));
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
    public Optional<SpendEntity> findByUsernameAndDescription(String username, String description) {
        String query = "SELECT * FROM spend WHERE username = ? and description = ?";
        try (PreparedStatement preparedStatement = holder(spdUrlJdbc).connection()
                .prepareStatement(
                        query
                )) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, description);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.ofNullable(SpendEntityRowMapper.instance.mapRow(resultSet, 0));
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("resource")
    @Override
    public void delete(SpendEntity spend) {
        String query = "DELETE FROM spend WHERE id = ?";
        try (PreparedStatement preparedStatement = holder(spdUrlJdbc).connection()
                .prepareStatement(
                        query)) {
            preparedStatement.setObject(1, spend.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

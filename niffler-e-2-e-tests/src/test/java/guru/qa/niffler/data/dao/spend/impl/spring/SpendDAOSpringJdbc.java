package guru.qa.niffler.data.dao.spend.impl.spring;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.spend.SpendDAO;
import guru.qa.niffler.data.dao.spend.mapper.SpendEntityRowMapper;
import guru.qa.niffler.data.entity.spend.SpendEntity;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.*;
import java.util.*;

import static guru.qa.niffler.data.template.DataSources.dataSource;

public class SpendDAOSpringJdbc implements SpendDAO {
    private static final Config CFG = Config.getInstance();

    private final JdbcTemplate jdbcTemplate;

    public SpendDAOSpringJdbc() {
        jdbcTemplate = new JdbcTemplate(dataSource(CFG.spendJdbcUrl()));
    }

    @Override
    public SpendEntity create(SpendEntity spend) {
        String query = "INSERT INTO spend (username, spend_date, currency, amount, description, category_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, spend.getUsername());
            ps.setDate(2, spend.getSpendDate());
            ps.setString(3, spend.getCurrency().name());
            ps.setDouble(4, spend.getAmount());
            ps.setString(5, spend.getDescription());
            ps.setObject(6, spend.getCategory().getId());
            return ps;
        }, keyHolder);
        UUID generatedKey = (UUID) keyHolder.getKeys().get("id");
        spend.setId(generatedKey);
        return spend;
    }

    @Override
    public Optional<SpendEntity> findById(UUID id) {
        String query = "SELECT * FROM spend WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    query, SpendEntityRowMapper.instance, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<SpendEntity> findAllByUsername(String username) {
        String query = "SELECT * FROM spend WHERE username = ?";
        try {
            return jdbcTemplate.query(
                    query, SpendEntityRowMapper.instance, username);
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public Optional<SpendEntity> findByUsernameAndDescription(String username, String description) {
        String query = "SELECT * FROM spend WHERE username = ? and description = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    query, SpendEntityRowMapper.instance, username, description));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<SpendEntity> findAll() {
        String query = "SELECT * FROM spend";
        try {
            return jdbcTemplate.query(query, SpendEntityRowMapper.instance);
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public void delete(SpendEntity spend) {
        String query = "DELETE FROM spend WHERE id = ?";
        jdbcTemplate.update(query, spend.getId());
    }
}

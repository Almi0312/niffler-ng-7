package guru.qa.niffler.data.dao.spend.impl.spring;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.spend.CategoryDAO;
import guru.qa.niffler.data.dao.spend.mapper.CategoryEntityRowMapper;
import guru.qa.niffler.data.entity.spend.CategoryEntity;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.*;
import java.util.*;

import static guru.qa.niffler.data.template.DataSources.dataSource;

public class CategoryDAOSpringJdbc implements CategoryDAO {
    private static final Config CFG = Config.getInstance();

    private final JdbcTemplate jdbcTemplate;

    public CategoryDAOSpringJdbc() {
        jdbcTemplate = new JdbcTemplate(dataSource(CFG.spendJdbcUrl()));
    }

    @Override
    public CategoryEntity create(CategoryEntity category) {
        String query = "INSERT INTO category (username, name, archived) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, category.getUsername());
            ps.setString(2, category.getName());
            ps.setBoolean(3, category.isArchived());
            return ps;
        }, keyHolder);
        final UUID generatedKey = (UUID) keyHolder.getKeys().get("id");
        category.setId(generatedKey);
        return category;
    }

    @Override
    public Optional<CategoryEntity> findById(UUID id) {
        String query = "SELECT * FROM category WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    query, CategoryEntityRowMapper.instance, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<CategoryEntity> findByUsernameAndName(String username, String categoryName) {
        String query = "SELECT * FROM category WHERE username = ? and name = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    query, CategoryEntityRowMapper.instance, username, categoryName));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<CategoryEntity> findAllByUsername(String username) {
        String query = "SELECT * FROM category WHERE username = ?";
        try {
            return jdbcTemplate.query(query, CategoryEntityRowMapper.instance, username);
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public List<CategoryEntity> findAll() {
        String query = "SELECT * FROM category";
        try {
            return jdbcTemplate.query(query, CategoryEntityRowMapper.instance);
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public CategoryEntity update(CategoryEntity category) {
        String query = "UPDATE category SET username = ?, name = ?, archived = ? WHERE id = ?";
        int updateRows = jdbcTemplate.update(query,
                category.getUsername(),
                category.getName(),
                category.isArchived(),
                category.getId());
        if (updateRows == 0) {
            throw new RuntimeException();
        }
        return category;
    }

    @Override
    public void delete(CategoryEntity category) {
        String query = "DELETE FROM category WHERE id = ?";
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setObject(1, category.getId());
            return ps;
        });
    }
}
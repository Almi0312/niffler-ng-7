package guru.qa.niffler.data.repository.spend.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.spend.CategoryDAO;
import guru.qa.niffler.data.dao.spend.SpendDAO;
import guru.qa.niffler.data.dao.spend.impl.spring.CategoryDAOSpringJdbc;
import guru.qa.niffler.data.dao.spend.impl.spring.SpendDAOSpringJdbc;
import guru.qa.niffler.data.dao.spend.mapper.SpendEntityRowMapper;
import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.entity.spend.SpendEntity;
import guru.qa.niffler.data.repository.spend.SpendRepository;
import guru.qa.niffler.data.repository.spend.mapper.SpendEntityExtractor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;

import static guru.qa.niffler.data.template.DataSources.dataSource;

public class SpendSpringRepositoryJdbc implements SpendRepository {
    private static final Config CFG = Config.getInstance();

    private final JdbcTemplate jdbcTemplate;
    private final SpendDAO spendDAO;
    private final CategoryDAO categoryDAO;

    public SpendSpringRepositoryJdbc() {
        jdbcTemplate = new JdbcTemplate(dataSource(CFG.spendJdbcUrl()));
        spendDAO = new SpendDAOSpringJdbc();
        categoryDAO = new CategoryDAOSpringJdbc();
    }

    @Override
    public SpendEntity createSpend(SpendEntity spend) {
        if (spend.getCategory().getId() == null) {
            CategoryEntity category = findCategoryByUsernameAndName(spend.getCategory().getUsername(),
                    spend.getCategory().getName()).orElseGet(() -> categoryDAO.create(spend.getCategory()));
            spend.setCategory(category);
        }
        return spendDAO.create(spend);
    }

    @Override
    public SpendEntity updateSpend(SpendEntity spend) {
        return spendDAO.update(spend);
    }

    @Override
    public Optional<SpendEntity> findById(UUID id) {
        String query = "SELECT * FROM spend s JOIN category c " +
                "ON (s.category_id = c.id) WHERE s.id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.query(
                    query, SpendEntityExtractor.instance, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<SpendEntity> findAllByUsername(String username) {
        String query = "SELECT * FROM spend s JOIN category c " +
                "ON (s.category_id = c.id) WHERE s.username = ?";
        return jdbcTemplate.query(query, SpendEntityRowMapper.instance, username);
    }

    @Override
    public Optional<SpendEntity> findByUsernameAndDescription(String username, String description) {
        String query = "SELECT * FROM spend s JOIN category c" +
                " ON (s.category_id = c.id) WHERE s.username = ? and s.description = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.query(
                    query, SpendEntityExtractor.instance, username, description));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<SpendEntity> findAll() {
        String query = "SELECT * FROM spend s JOIN category c " +
                "ON (s.category_id = c.id)";
        try {
            return jdbcTemplate.query(query, SpendEntityRowMapper.instance);
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public void remove(SpendEntity spend) {
        spendDAO.delete(spend);
        categoryDAO.delete(spend.getCategory());
    }

    @Override
    public CategoryEntity createCategory(CategoryEntity category) {
        return categoryDAO.create(category);
    }

    @Override
    public Optional<CategoryEntity> findCategoryById(UUID id) {
        return categoryDAO.findById(id);
    }

    @Override
    public Optional<CategoryEntity> findCategoryByUsernameAndName(String username, String categoryName) {
        return categoryDAO.findByUsernameAndName(username, categoryName);
    }

    @Override
    public List<CategoryEntity> findAllCategoryByUsername(String username) {
        return categoryDAO.findAllByUsername(username);
    }

    @Override
    public List<CategoryEntity> findAllCategory() {
        return categoryDAO.findAll();
    }

    @Override
    public CategoryEntity updateCategory(CategoryEntity category) {
        return categoryDAO.update(category);
    }

    @Override
    public void removeCategory(CategoryEntity category) {
        categoryDAO.delete(category);
    }
}

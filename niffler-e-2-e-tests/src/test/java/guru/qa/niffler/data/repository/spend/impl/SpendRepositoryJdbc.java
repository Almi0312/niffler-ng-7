package guru.qa.niffler.data.repository.spend.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.spend.CategoryDAO;
import guru.qa.niffler.data.dao.spend.SpendDAO;
import guru.qa.niffler.data.dao.spend.impl.default_jdbc.CategoryDAOJdbc;
import guru.qa.niffler.data.dao.spend.impl.default_jdbc.SpendDAOJdbc;
import guru.qa.niffler.data.dao.spend.mapper.SpendEntityRowMapper;
import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.entity.spend.SpendEntity;
import guru.qa.niffler.data.repository.spend.SpendRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static guru.qa.niffler.data.template.Connections.holder;

public class SpendRepositoryJdbc implements SpendRepository {

    private static final Config CFG = Config.getInstance();

    private final String spendJdbcUrl = CFG.spendJdbcUrl();
    private final SpendDAO spendDAO = new SpendDAOJdbc();
    private final CategoryDAO categoryDAO = new CategoryDAOJdbc();

    @Override
    public SpendEntity create(SpendEntity spend) {
        if (spend.getCategory().getId() == null) {
            CategoryEntity category = categoryDAO.create(spend.getCategory());
            spend.setCategory(category);
        }
        return spendDAO.create(spend);
    }

    @Override
    public Optional<SpendEntity> findById(UUID id) {
        return spendDAO.findById(id);
    }

    @Override
    public List<SpendEntity> findAllByUsername(String username) {
        String query = "SELECT * FROM spend s JOIN category c " +
                "ON (s.category_id = c.id) WHERE username = ?";
        try (PreparedStatement ps = holder(spendJdbcUrl).connection().prepareStatement(query)) {
            ps.setString(1, username);
            try (ResultSet resultSet = ps.executeQuery()) {
                List<SpendEntity> spends = new ArrayList<>();
                while (resultSet.next()) {
                    spends.add(SpendEntityRowMapper.instance.mapRow(resultSet, 0));
                }
                return spends;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<SpendEntity> findAll() {
        String query = "SELECT * FROM spend s JOIN category c " +
                "ON (s.category_id = c.id)";
        try (PreparedStatement ps = holder(spendJdbcUrl).connection().prepareStatement(query)) {
            try (ResultSet resultSet = ps.executeQuery()) {
                List<SpendEntity> spends = new ArrayList<>();
                while (resultSet.next()) {
                    spends.add(SpendEntityRowMapper.instance.mapRow(resultSet, 0));
                }
                return spends;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<SpendEntity> findByUsernameAndDescription(String username, String description) {
        String query = "SELECT * FROM spend s JOIN category c" +
                " ON (s.category_id = c.id) WHERE s.username = ? AND s.description = ?";
        try (PreparedStatement ps = holder(spendJdbcUrl).connection().prepareStatement(query)) {
            ps.setString(1, username);
            ps.setString(2, description);
            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.ofNullable(SpendEntityRowMapper.instance.mapRow(resultSet, 0));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(SpendEntity spend) {
        CategoryEntity category = spend.getCategory();
        spendDAO.delete(spend);
        categoryDAO.delete(category);
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
    public void deleteCategory(CategoryEntity category) {
        categoryDAO.delete(category);
    }

}

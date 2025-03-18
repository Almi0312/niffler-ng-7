package guru.qa.niffler.service.spend;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.spend.CategoryDAO;
import guru.qa.niffler.data.dao.spend.SpendDAO;
import guru.qa.niffler.data.dao.spend.impl.default_jdbc.CategoryDAOJdbc;
import guru.qa.niffler.data.dao.spend.impl.default_jdbc.SpendDAOJdbc;
import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.entity.spend.SpendEntity;
import guru.qa.niffler.data.template.JdbcTransactionTemplate;
import guru.qa.niffler.model.SpendJson;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class SpendDBClient {

    private static final Config CFG = Config.getInstance();

    private final CategoryDAO categoryDAOJdbc;
    private final SpendDAO spendDAOJdbc;
    private final JdbcTransactionTemplate txTemplate;

    public SpendDBClient() {
        this.categoryDAOJdbc = new CategoryDAOJdbc();
        this.spendDAOJdbc = new SpendDAOJdbc();
        this.txTemplate = new JdbcTransactionTemplate(CFG.spendJdbcUrl());
    }

    public SpendJson create(SpendJson json) {
        return txTemplate.execute(Connection.TRANSACTION_READ_UNCOMMITTED,
                () -> {
                    SpendEntity spendEntity = SpendEntity.fromJson(json);
                    if (spendEntity.getCategory().getId() == null) {
                        CategoryEntity categoryEntity = categoryDAOJdbc
                                .create(spendEntity.getCategory());
                        spendEntity.setCategory(categoryEntity);
                    }
                    return SpendJson.fromEntity(spendDAOJdbc.create(spendEntity));
                });
    }

    public Optional<SpendJson> findById(UUID id) {
        return txTemplate.execute(Connection.TRANSACTION_READ_COMMITTED,
                () -> {
                    Optional<SpendEntity> spend = spendDAOJdbc.findById(id);
                    spend.ifPresent(spendEntity -> {
                        categoryDAOJdbc.findById(spendEntity.getCategory().getId())
                                .ifPresent(spendEntity::setCategory);
                    });
                    return spend.map(SpendJson::fromEntity);
                });
    }

    public List<SpendJson> findAllByUsername(String username) {
        return txTemplate.execute(Connection.TRANSACTION_READ_COMMITTED,
                () -> {
                    List<SpendEntity> spends = spendDAOJdbc.findAllByUsername(username);
                    spends.forEach(spend ->
                            categoryDAOJdbc.findById(spend.getCategory().getId())
                                    .ifPresent(spend::setCategory));
                    return spends.stream().map(SpendJson::fromEntity).toList();
                });
    }

    public Optional<SpendJson> findByUsernameAndDescription(String username, String name) {
        return txTemplate.execute(Connection.TRANSACTION_READ_COMMITTED,
                () -> {
                    Optional<SpendEntity> spend = spendDAOJdbc.findByUsernameAndDescription(username, name);
                    spend.ifPresent(spendEnt ->
                            categoryDAOJdbc.findById(spendEnt.getCategory().getId())
                                    .ifPresent(spendEnt::setCategory));
                    return spend.map(SpendJson::fromEntity);
                });
    }

    public void delete(SpendJson spendJson) {
        txTemplate.execute(Connection.TRANSACTION_READ_COMMITTED,
                con -> {
                    SpendEntity spendEntity = SpendEntity.fromJson(spendJson);
                    CategoryEntity categoryEntity = spendEntity.getCategory();
                    spendDAOJdbc
                            .delete(SpendEntity.fromJson(spendJson));
                    if (categoryEntity.getId() != null) {
                        categoryDAOJdbc.delete(categoryEntity);
                    }
                });
    }

}
package guru.qa.niffler.service.spend;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.spend.impl.default_jdbc.CategoryDAOJdbc;
import guru.qa.niffler.data.dao.spend.impl.default_jdbc.SpendDAOJdbc;
import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.entity.spend.SpendEntity;
import guru.qa.niffler.data.template.JdbcTransactionTemplate;
import guru.qa.niffler.model.SpendJson;

import javax.annotation.Nullable;
import java.sql.Connection;

public class SpendDBSpringClient {

    private static final Config CFG = Config.getInstance();

    private final CategoryDAOJdbc categoryDAOJdbc;
    private final SpendDAOJdbc spendDAOJdbc;
    private final JdbcTransactionTemplate txTemplate;

    public SpendDBSpringClient() {
        this.categoryDAOJdbc = new CategoryDAOJdbc();
        this.spendDAOJdbc = new SpendDAOJdbc();
        this.txTemplate = new JdbcTransactionTemplate(CFG.spendJdbcUrl());
    }

    public SpendJson create(SpendJson spendJson) {
        return txTemplate.execute(Connection.TRANSACTION_READ_UNCOMMITTED,
                () -> {
                    SpendEntity spendEntity = SpendEntity.fromJson(spendJson);
                    if (spendEntity.getCategory().getId() == null) {
                        CategoryEntity categoryEntity = categoryDAOJdbc.create(spendEntity.getCategory());
                        spendEntity.setCategory(categoryEntity);
                    }
                    return SpendJson.fromEntity(spendDAOJdbc.create(spendEntity));
                });
    }

    public @Nullable SpendJson findByUsernameAndDescription(String username, String description) {
        return txTemplate.execute(Connection.TRANSACTION_READ_COMMITTED,
                () -> {
                    SpendEntity spendEntity = spendDAOJdbc
                            .findByUsernameAndDescription(username, description).orElse(null);
                    if (spendEntity != null) {
                        categoryDAOJdbc.findById(spendEntity.getCategory().getId())
                                .ifPresent(entity ->
                                        categoryDAOJdbc.findById(spendEntity.getCategory().getId())
                                                .ifPresent(spendEntity::setCategory));
                    }
                    return SpendJson.fromEntity(spendEntity);
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

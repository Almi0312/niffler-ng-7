package guru.qa.niffler.service.spend;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.spend.CategoryDAO;
import guru.qa.niffler.data.dao.spend.SpendDAO;
import guru.qa.niffler.data.dao.spend.impl.spring.CategoryDAOSpringJdbc;
import guru.qa.niffler.data.dao.spend.impl.spring.SpendDAOSpringJdbc;
import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.entity.spend.SpendEntity;
import guru.qa.niffler.data.template.JdbcTransactionTemplate;
import guru.qa.niffler.model.SpendJson;

import java.sql.Connection;
import java.util.Optional;

public class SpendDBSpringClient {

    private static final Config CFG = Config.getInstance();

    private final CategoryDAO categoryDAOJdbc;
    private final SpendDAO spendDAOJdbc;
    private final JdbcTransactionTemplate txTemplate;

    public SpendDBSpringClient() {
        this.categoryDAOJdbc = new CategoryDAOSpringJdbc();
        this.spendDAOJdbc = new SpendDAOSpringJdbc();
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

    public Optional<SpendJson> findByUsernameAndDescription(String username, String description) {
        return txTemplate.execute(Connection.TRANSACTION_READ_COMMITTED,
                () -> {
                    Optional<SpendEntity> spendEntity = spendDAOJdbc.findByUsernameAndDescription(username, description);
                    spendEntity.ifPresent(spend ->
                            categoryDAOJdbc.findById(spend.getCategory().getId())
                                    .ifPresent(spend::setCategory));
                    return spendEntity.map(SpendJson::fromEntity);
                });
    }

    public void delete(SpendJson spendJson) {
        txTemplate.execute(Connection.TRANSACTION_READ_COMMITTED,
                con -> {
                    SpendEntity spendEntity = SpendEntity.fromJson(spendJson);
                    CategoryEntity categoryEntity = spendEntity.getCategory();
                    spendDAOJdbc.delete(SpendEntity.fromJson(spendJson));
                    if (categoryEntity.getId() != null) {
                        categoryDAOJdbc.delete(categoryEntity);
                    }
                });
    }
}

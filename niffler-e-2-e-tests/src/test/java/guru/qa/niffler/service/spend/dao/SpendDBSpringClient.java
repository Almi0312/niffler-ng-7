package guru.qa.niffler.service.spend.dao;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.spend.CategoryDAO;
import guru.qa.niffler.data.dao.spend.SpendDAO;
import guru.qa.niffler.data.dao.spend.impl.spring.CategoryDAOSpringJdbc;
import guru.qa.niffler.data.dao.spend.impl.spring.SpendDAOSpringJdbc;
import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.entity.spend.SpendEntity;
import guru.qa.niffler.data.template.DataSources;
import guru.qa.niffler.model.SpendJson;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.util.Optional;

public class SpendDBSpringClient {

    private static final Config CFG = Config.getInstance();

    private final CategoryDAO categoryDAOSpringJdbc;
    private final SpendDAO spendDAOSpringJdbc;
    private final TransactionTemplate txTemplate;

    public SpendDBSpringClient() {
        this.categoryDAOSpringJdbc = new CategoryDAOSpringJdbc();
        this.spendDAOSpringJdbc = new SpendDAOSpringJdbc();
        this.txTemplate = new TransactionTemplate(
                new JdbcTransactionManager(
                        DataSources.dataSource(CFG.spendJdbcUrl())));
    }

    public SpendJson create(SpendJson spendJson) {
        txTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
        return txTemplate.execute(con -> {
                    SpendEntity spendEntity = SpendEntity.fromJson(spendJson);
                    if (spendEntity.getCategory().getId() == null) {
                        CategoryEntity categoryEntity = categoryDAOSpringJdbc
                                .create(spendEntity.getCategory());
                        spendEntity.setCategory(categoryEntity);
                    }
                    return SpendJson.fromEntity(spendDAOSpringJdbc.create(spendEntity));
                });
    }

    public Optional<SpendJson> findByUsernameAndDescription(String username, String description) {
        txTemplate.setIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
        return txTemplate.execute(con -> {
                    Optional<SpendEntity> spendEntity = spendDAOSpringJdbc.findByUsernameAndDescription(username, description);
                    spendEntity.ifPresent(spend ->
                            categoryDAOSpringJdbc.findById(spend.getCategory().getId())
                                    .ifPresent(spend::setCategory));
                    return spendEntity.map(SpendJson::fromEntity);
                });
    }

    public void delete(SpendJson spendJson) {
        txTemplate.setIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
        txTemplate.executeWithoutResult(con -> {
                    SpendEntity spendEntity = SpendEntity.fromJson(spendJson);
                    CategoryEntity categoryEntity = spendEntity.getCategory();
                    spendDAOSpringJdbc.delete(SpendEntity.fromJson(spendJson));
                    if (categoryEntity.getId() != null) {
                        categoryDAOSpringJdbc.delete(categoryEntity);
                    }
                });
    }
}

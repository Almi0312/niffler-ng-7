package guru.qa.niffler.service.spend.dao;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.spend.CategoryDAO;
import guru.qa.niffler.data.dao.spend.impl.spring.CategoryDAOSpringJdbc;
import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.template.DataSources;
import guru.qa.niffler.model.CategoryJson;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CategoryDBSpringClient {

    private static final Config CFG = Config.getInstance();

    private final CategoryDAO categoryDAOJdbc;
    private final TransactionTemplate txTemplate;

    public CategoryDBSpringClient() {
        this.categoryDAOJdbc = new CategoryDAOSpringJdbc();
        this.txTemplate = new TransactionTemplate(
                new JdbcTransactionManager(
                        DataSources.dataSource(CFG.spendJdbcUrl())));
    }

    public CategoryJson create(CategoryJson categoryJson) {
        txTemplate.setIsolationLevel(Connection.TRANSACTION_READ_UNCOMMITTED);
        return CategoryJson.fromEntity(
                txTemplate.execute(con ->
                        categoryDAOJdbc.create(CategoryEntity.fromJson(categoryJson))));
    }

    public Optional<CategoryJson> findById(UUID id) {
        txTemplate.setIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
        return txTemplate.execute(con ->
                categoryDAOJdbc.findById(id)
                        .map(CategoryJson::fromEntity));
    }

    public Optional<CategoryJson> findByUsernameAndName(String username, String categoryName) {
        txTemplate.setIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
        return txTemplate.execute(con ->
                categoryDAOJdbc.findByUsernameAndName(username, categoryName)
                        .map(CategoryJson::fromEntity));
    }

    public List<CategoryJson> findAll() {
        txTemplate.setIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
        return txTemplate.execute(con ->
                categoryDAOJdbc.findAll().stream()
                        .map(CategoryJson::fromEntity).toList());
    }

    public void delete(CategoryJson categoryJson) {
        txTemplate.setIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
        txTemplate.executeWithoutResult(con ->
                categoryDAOJdbc.delete(CategoryEntity.fromJson(categoryJson)));
    }
}

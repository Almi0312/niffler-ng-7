package guru.qa.niffler.service.spend;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.spend.impl.default_jdbc.CategoryDAOJdbc;
import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.template.JdbcTransactionTemplate;
import guru.qa.niffler.model.CategoryJson;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CategoryDBSpringClient {

    private static final Config CFG = Config.getInstance();

    private final CategoryDAOJdbc categoryDAOJdbc;
    private final JdbcTransactionTemplate txTemplate;

    public CategoryDBSpringClient() {
        this.categoryDAOJdbc = new CategoryDAOJdbc();
        this.txTemplate = new JdbcTransactionTemplate(CFG.spendJdbcUrl());
    }

    public CategoryJson create(CategoryJson categoryJson) {
        return CategoryJson.fromEntity(
                txTemplate.execute(Connection.TRANSACTION_READ_UNCOMMITTED,
                        () -> categoryDAOJdbc
                                .create(CategoryEntity.fromJson(categoryJson))));
    }

    public Optional<CategoryJson> findById(UUID id) {
        return txTemplate.execute(Connection.TRANSACTION_READ_COMMITTED,
                () -> categoryDAOJdbc.findById(id)
                        .map(CategoryJson::fromEntity));
    }

    public Optional<CategoryJson> findByUsernameAndName(String username, String categoryName) {
        return txTemplate.execute(Connection.TRANSACTION_READ_COMMITTED,
                () -> categoryDAOJdbc
                        .findByUsernameAndName(username, categoryName).map(CategoryJson::fromEntity));
    }

    public List<CategoryJson> findAll() {
        return txTemplate.execute(Connection.TRANSACTION_READ_COMMITTED,
                () -> categoryDAOJdbc.findAll().stream()
                        .map(CategoryJson::fromEntity).toList());
    }

    public void delete(CategoryJson categoryJson) {
        txTemplate.execute(Connection.TRANSACTION_READ_COMMITTED,
                con -> categoryDAOJdbc
                        .delete(CategoryEntity.fromJson(categoryJson)));
    }
}

package guru.qa.niffler.service.spend.dao;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.spend.impl.default_jdbc.CategoryDAOJdbc;
import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.template.JdbcTransactionTemplate;
import guru.qa.niffler.model.CategoryJson;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CategoryDBClient {

    private static final Config CFG = Config.getInstance();

    private final CategoryDAOJdbc categoryDAOJdbc;
    private final JdbcTransactionTemplate txTemplate;

    public CategoryDBClient() {
        this.categoryDAOJdbc = new CategoryDAOJdbc();
        this.txTemplate = new JdbcTransactionTemplate(CFG.spendJdbcUrl());
    }

    public CategoryJson createCategory(CategoryJson categoryJson) {
        return txTemplate.execute(Connection.TRANSACTION_READ_UNCOMMITTED,
                () -> CategoryJson.fromEntity(categoryDAOJdbc
                        .create(CategoryEntity.fromJson(categoryJson))));
    }

    public Optional<CategoryJson> findById(UUID id) {
        return txTemplate.execute(Connection.TRANSACTION_READ_COMMITTED,
                () -> categoryDAOJdbc.findById(id)
                        .map(CategoryJson::fromEntity));
    }

    public List<CategoryJson> findAllByUsername(String username) {
        return txTemplate.execute(Connection.TRANSACTION_READ_COMMITTED,
                () -> categoryDAOJdbc.findAllByUsername(username).stream()
                        .map(CategoryJson::fromEntity).toList());
    }

    public Optional<CategoryJson> findByUsernameAndName(String username, String categoryName) {
        return txTemplate.execute(Connection.TRANSACTION_READ_COMMITTED,
                () -> categoryDAOJdbc.findByUsernameAndName(username, categoryName)
                        .map(CategoryJson::fromEntity));
    }

    public CategoryJson updateCategory(CategoryJson categoryJson) {
        return txTemplate.execute(Connection.TRANSACTION_SERIALIZABLE,
                () -> CategoryJson.fromEntity(categoryDAOJdbc
                        .update(CategoryEntity.fromJson(categoryJson))));
    }

    public void delete(CategoryJson categoryJson) {
        txTemplate.execute(Connection.TRANSACTION_SERIALIZABLE,
                con -> categoryDAOJdbc
                        .delete(CategoryEntity.fromJson(categoryJson)));
    }
}

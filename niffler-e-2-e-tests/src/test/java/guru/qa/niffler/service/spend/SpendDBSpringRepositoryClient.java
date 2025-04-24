package guru.qa.niffler.service.spend;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.entity.spend.SpendEntity;
import guru.qa.niffler.data.repository.spend.SpendRepository;
import guru.qa.niffler.data.repository.spend.impl.SpendSpringRepositoryJdbc;
import guru.qa.niffler.data.jdbc.DataSources;
import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.model.SpendJson;
import guru.qa.niffler.service.SpendsClient;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class SpendDBSpringRepositoryClient implements SpendsClient {

    private static final Config CFG = Config.getInstance();

    private final SpendRepository spendRepo;
    private final TransactionTemplate txTemplate;

    public SpendDBSpringRepositoryClient() {
        this.spendRepo = new SpendSpringRepositoryJdbc();
        this.txTemplate = new TransactionTemplate(
                new JdbcTransactionManager(
                        DataSources.dataSource(CFG.spendJdbcUrl())));
    }

    public SpendJson createSpend(SpendJson spend) {
        txTemplate.setIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
        return txTemplate.execute(con ->
                SpendJson.fromEntity(spendRepo.createSpend(SpendEntity.fromJson(spend))));
    }

    @Override
    public SpendJson updateSpend(SpendJson spend) {
        txTemplate.setIsolationLevel(Connection.TRANSACTION_REPEATABLE_READ);
        return txTemplate.execute(con ->
                SpendJson.fromEntity(spendRepo.updateSpend(SpendEntity.fromJson(spend))));
    }

    @Override
    public Optional<SpendJson> findById(UUID id) {
        txTemplate.setIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
        return txTemplate.execute(con ->
                spendRepo.findById(id).map(SpendJson::fromEntity));
    }

    @Override
    public List<SpendJson> findAllByUsername(String username) {
        txTemplate.setIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
        return txTemplate.execute(con ->
                spendRepo.findAllByUsername(username).stream()
                        .map(SpendJson::fromEntity)
                        .toList());
    }

    @Override
    public Optional<SpendJson> findByUsernameAndDescription(String username, String name) {
        txTemplate.setIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
        return txTemplate.execute(con ->
                spendRepo.findByUsernameAndDescription(username, name).map(SpendJson::fromEntity));
    }

    @Override
    public List<SpendJson> findAll() {
        txTemplate.setIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
        return txTemplate.execute(con ->
                spendRepo.findAll().stream().map(SpendJson::fromEntity).toList());
    }

    @Override
    public void remove(SpendJson spend) {
        txTemplate.setIsolationLevel(Connection.TRANSACTION_REPEATABLE_READ);
        txTemplate.executeWithoutResult(con -> spendRepo.remove(SpendEntity.fromJson(spend)));
    }

    @Override
    public CategoryJson createCategory(CategoryJson category) {
        txTemplate.setIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
        return txTemplate.execute(con ->
                CategoryJson.fromEntity(spendRepo.findCategoryByUsernameAndName(category.username(), category.name())
                        .orElseGet(() -> spendRepo.createCategory(CategoryEntity.fromJson(category)))));
    }

    @Override
    public CategoryJson updateCategory(CategoryJson category) {
        txTemplate.setIsolationLevel(Connection.TRANSACTION_REPEATABLE_READ);
        return txTemplate.execute(con ->
                CategoryJson.fromEntity(spendRepo.updateCategory(CategoryEntity.fromJson(category))));
    }

    @Override
    public Optional<CategoryJson> findCategoryById(UUID id) {
        txTemplate.setIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
        return txTemplate.execute(con ->
                spendRepo.findCategoryById(id).map(CategoryJson::fromEntity));
    }

    @Override
    public Optional<CategoryJson> findCategoryByUsernameAndName(String username, String categoryName) {
        txTemplate.setIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
        return txTemplate.execute(con ->
                spendRepo.findCategoryByUsernameAndName(username, categoryName)
                        .map(CategoryJson::fromEntity));
    }

    @Override
    public List<CategoryJson> findAllCategoryByUsername(String username) {
        txTemplate.setIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
        return txTemplate.execute(con ->
                spendRepo.findAllCategoryByUsername(username).stream()
                        .map(CategoryJson::fromEntity).toList());
    }

    @Override
    public List<CategoryJson> findAllCategory() {
        txTemplate.setIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
        return txTemplate.execute(con ->
                spendRepo.findAllCategory().stream()
                        .map(CategoryJson::fromEntity).toList());
    }

    @Override
    public void removeCategory(CategoryJson category) {
        txTemplate.setIsolationLevel(Connection.TRANSACTION_REPEATABLE_READ);
        txTemplate.executeWithoutResult(con ->
                spendRepo.removeCategory(CategoryEntity.fromJson(category)));
    }

}
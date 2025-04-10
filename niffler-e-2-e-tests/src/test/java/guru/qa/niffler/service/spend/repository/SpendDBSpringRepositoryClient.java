package guru.qa.niffler.service.spend.repository;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.entity.spend.SpendEntity;
import guru.qa.niffler.data.repository.spend.SpendRepository;
import guru.qa.niffler.data.repository.spend.impl.SpendSpringRepositoryJdbc;
import guru.qa.niffler.data.template.DataSources;
import guru.qa.niffler.model.SpendJson;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class SpendDBSpringRepositoryClient {

    private static final Config CFG = Config.getInstance();

    private final SpendRepository spendRepo;
    private final TransactionTemplate txTemplate;

    public SpendDBSpringRepositoryClient() {
        this.spendRepo = new SpendSpringRepositoryJdbc();
        this.txTemplate = new TransactionTemplate(
                new JdbcTransactionManager(
                        DataSources.dataSource(CFG.spendJdbcUrl())));
    }

    public SpendJson create(SpendJson json) {
        txTemplate.setIsolationLevel(Connection.TRANSACTION_READ_UNCOMMITTED);
        return
                txTemplate.execute(con -> {
                    SpendEntity spend = SpendEntity.fromJson(json);
                    if (spend.getCategory().getId() == null) {
                        CategoryEntity category = spendRepo.createCategory(spend.getCategory());
                        spend.setCategory(category);
                    }
                    return SpendJson.fromEntity(spendRepo.createSpend(spend));
                });
    }

    public Optional<SpendJson> findById(UUID id) {
        txTemplate.setIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
        return txTemplate.execute(con -> spendRepo.findById(id).map(SpendJson::fromEntity));
    }

    public List<SpendJson> findAllByUsername(String username) {
        txTemplate.setIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
        return txTemplate.execute(con ->
                spendRepo.findAllByUsername(username).stream()
                        .map(SpendJson::fromEntity)
                        .toList());
    }

    public Optional<SpendJson> findByUsernameAndDescription(String username, String name) {
        txTemplate.setIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
        return txTemplate.execute(con -> spendRepo.findByUsernameAndDescription(username, name).map(SpendJson::fromEntity));
    }

    public void delete(SpendJson spendJson) {
        txTemplate.setIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
        txTemplate.executeWithoutResult(con -> spendRepo.delete(SpendEntity.fromJson(spendJson)));
    }

}
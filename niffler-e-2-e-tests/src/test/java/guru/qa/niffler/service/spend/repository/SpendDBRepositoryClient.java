package guru.qa.niffler.service.spend.repository;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.spend.SpendEntity;
import guru.qa.niffler.data.repository.spend.SpendRepository;
import guru.qa.niffler.data.repository.spend.impl.SpendRepositoryJdbc;
import guru.qa.niffler.data.template.JdbcTransactionTemplate;
import guru.qa.niffler.model.SpendJson;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class SpendDBRepositoryClient {

    private static final Config CFG = Config.getInstance();

    private final SpendRepository spendRepo;
    private final JdbcTransactionTemplate txTemplate;

    public SpendDBRepositoryClient() {
        this.spendRepo = new SpendRepositoryJdbc();
        this.txTemplate = new JdbcTransactionTemplate(CFG.spendJdbcUrl());
    }

    public SpendJson create(SpendJson json) {
        return SpendJson.fromEntity(
                txTemplate.execute(Connection.TRANSACTION_READ_UNCOMMITTED,
                        () -> spendRepo.createSpend(SpendEntity.fromJson(json))));
    }

    public Optional<SpendJson> findById(UUID id) {
        return txTemplate.execute(Connection.TRANSACTION_READ_COMMITTED,
                () -> spendRepo.findById(id).map(SpendJson::fromEntity));
    }

    public List<SpendJson> findAllByUsername(String username) {
        return txTemplate.execute(Connection.TRANSACTION_READ_COMMITTED,
                () -> spendRepo.findAllByUsername(username).stream()
                        .map(SpendJson::fromEntity)
                        .toList());
    }

    public Optional<SpendJson> findByUsernameAndDescription(String username, String name) {
        return txTemplate.execute(Connection.TRANSACTION_READ_COMMITTED,
                () -> spendRepo.findByUsernameAndDescription(username, name).map(SpendJson::fromEntity));
    }

    public void delete(SpendJson spendJson) {
        txTemplate.execute(Connection.TRANSACTION_READ_COMMITTED,
                con -> spendRepo.delete(SpendEntity.fromJson(spendJson)));
    }

}
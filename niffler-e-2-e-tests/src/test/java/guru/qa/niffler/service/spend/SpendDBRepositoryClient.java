package guru.qa.niffler.service.spend;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.entity.spend.SpendEntity;
import guru.qa.niffler.data.repository.spend.SpendRepository;
import guru.qa.niffler.data.repository.spend.impl.SpendRepositoryJdbc;
import guru.qa.niffler.data.template.JdbcTransactionTemplate;
import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.model.SpendJson;
import guru.qa.niffler.service.SpendsClient;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
public class SpendDBRepositoryClient implements SpendsClient {

    private static final Config CFG = Config.getInstance();

    private final SpendRepository spendRepo;
    private final JdbcTransactionTemplate txTemplate;

    public SpendDBRepositoryClient() {
        this.spendRepo = new SpendRepositoryJdbc();
        this.txTemplate = new JdbcTransactionTemplate(CFG.spendJdbcUrl());
    }

    @Override
    public @Nonnull SpendJson createSpend(SpendJson spend) {
        return txTemplate.execute(() ->
            SpendJson.fromEntity(spendRepo.createSpend(SpendEntity.fromJson(spend))));
    }

    @Override
    public @Nonnull SpendJson updateSpend(SpendJson spend) {
        return txTemplate.execute(Connection.TRANSACTION_REPEATABLE_READ,
                () -> SpendJson.fromEntity(spendRepo.updateSpend(SpendEntity.fromJson(spend))));
    }

    @Override
    public @Nonnull Optional<SpendJson> findById(UUID id) {
        return txTemplate.execute(() ->
                spendRepo.findById(id).map(SpendJson::fromEntity));
    }

    @Override
    public @Nonnull List<SpendJson> findAllByUsername(String username) {
        return txTemplate.execute(() ->
                spendRepo.findAllByUsername(username).stream()
                        .map(SpendJson::fromEntity)
                        .toList());
    }

    @Override
    public @Nonnull Optional<SpendJson> findByUsernameAndDescription(String username, String name) {
        return txTemplate.execute(() ->
                spendRepo.findByUsernameAndDescription(username, name).map(SpendJson::fromEntity));
    }

    @Override
    public @Nonnull List<SpendJson> findAll() {
        return txTemplate.execute(() ->
                spendRepo.findAll().stream().map(SpendJson::fromEntity).toList());
    }

    @Override
    public void remove(SpendJson spend) {
        txTemplate.execute(Connection.TRANSACTION_REPEATABLE_READ,
                con -> spendRepo.remove(SpendEntity.fromJson(spend)));
    }

    @Override
    public @Nonnull CategoryJson createCategory(CategoryJson category) {
        return txTemplate.execute(() ->
                CategoryJson.fromEntity(spendRepo.createCategory(CategoryEntity.fromJson(category))));
    }

    @Override
    public @Nonnull CategoryJson updateCategory(CategoryJson category) {
        return txTemplate.execute(Connection.TRANSACTION_REPEATABLE_READ,
                () -> CategoryJson.fromEntity(spendRepo.updateCategory(CategoryEntity.fromJson(category))));
    }

    @Override
    public @Nonnull Optional<CategoryJson> findCategoryById(UUID id) {
        return txTemplate.execute(() ->
                spendRepo.findCategoryById(id).map(CategoryJson::fromEntity));
    }

    @Override
    public @Nonnull Optional<CategoryJson> findCategoryByUsernameAndName(String username, String categoryName) {
        return txTemplate.execute(() ->
                spendRepo.findCategoryByUsernameAndName(username, categoryName)
                        .map(CategoryJson::fromEntity));
    }

    @Override
    public @Nonnull List<CategoryJson> findAllCategoryByUsername(String username) {
        return txTemplate.execute(() ->
                spendRepo.findAllCategoryByUsername(username).stream()
                        .map(CategoryJson::fromEntity).toList());
    }

    @Override
    public @Nonnull List<CategoryJson> findAllCategory() {
        return txTemplate.execute(() ->
                spendRepo.findAllCategory().stream()
                        .map(CategoryJson::fromEntity).toList());
    }

    @Override
    public void removeCategory(CategoryJson category) {
        txTemplate.execute(Connection.TRANSACTION_REPEATABLE_READ,
                con -> spendRepo.removeCategory(CategoryEntity.fromJson(category)));
    }
}
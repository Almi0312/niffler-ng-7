package guru.qa.niffler.data.repository.spend;

import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.entity.spend.SpendEntity;
import guru.qa.niffler.data.repository.spend.impl.SpendHibernateRepository;
import guru.qa.niffler.data.repository.spend.impl.SpendRepositoryJdbc;
import guru.qa.niffler.data.repository.spend.impl.SpendSpringRepositoryJdbc;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
public interface SpendRepository {
    @Nonnull
    static SpendRepository getInstance() {
        return switch (System.getProperty("repository.impl", "jpa")) {
            case "jpa" -> new SpendHibernateRepository();
            case "jdbc" -> new SpendRepositoryJdbc();
            case "sjdbc" -> new SpendSpringRepositoryJdbc();
            default -> throw new IllegalStateException("Unexpected value: " + System.getProperty("repository.impl"));
        };
    }

    @Nonnull
    SpendEntity createSpend(SpendEntity spend);

    @Nonnull
    SpendEntity updateSpend(SpendEntity spend);

    @Nonnull
    CategoryEntity createCategory(CategoryEntity category);

    @Nonnull
    Optional<CategoryEntity> findCategoryById(UUID id);

    @Nonnull
    Optional<SpendEntity> findById(UUID id);

    @Nonnull
    Optional<SpendEntity> findByUsernameAndDescription(String username, String name);

    @Nonnull
    List<SpendEntity> findAll();

    @Nonnull
    List<SpendEntity> findAllByUsername(String username);

    void remove(SpendEntity spend);

    @Nonnull
    Optional<CategoryEntity> findCategoryByUsernameAndName(String username, String categoryName);

    @Nonnull
    List<CategoryEntity> findAllCategoryByUsername(String username);

    @Nonnull
    List<CategoryEntity> findAllCategory();

    @Nonnull
    CategoryEntity updateCategory(CategoryEntity category);

    void removeCategory(CategoryEntity category);
}

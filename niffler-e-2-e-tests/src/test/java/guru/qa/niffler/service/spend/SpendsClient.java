package guru.qa.niffler.service.spend;

import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.model.SpendJson;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
public interface SpendsClient {

    @Nonnull
    SpendJson createSpend(SpendJson spend);

    @Nonnull
    SpendJson updateSpend(SpendJson spend);

    @Nonnull
    CategoryJson createCategory(CategoryJson category);

    @Nonnull
    Optional<CategoryJson> findCategoryById(UUID id);

    @Nonnull
    Optional<SpendJson> findById(UUID id);

    @Nonnull
    Optional<SpendJson> findByUsernameAndDescription(String username, String name);

    @Nonnull
    List<SpendJson> findAll();

    @Nonnull
    List<SpendJson> findAllByUsername(String username);

    void remove(SpendJson spend);

    @Nonnull
    Optional<CategoryJson> findCategoryByUsernameAndName(String username, String categoryName);

    @Nonnull
    List<CategoryJson> findAllCategoryByUsername(String username);

    @Nonnull
    List<CategoryJson> findAllCategory();

    @Nonnull
    CategoryJson updateCategory(CategoryJson category);

    void removeCategory(CategoryJson category);

}

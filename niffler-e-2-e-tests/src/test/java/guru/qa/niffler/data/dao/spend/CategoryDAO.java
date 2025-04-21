package guru.qa.niffler.data.dao.spend;

import guru.qa.niffler.data.entity.spend.CategoryEntity;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
public interface CategoryDAO {
    @Nonnull
    CategoryEntity create(CategoryEntity category);

    @Nonnull
    CategoryEntity update(CategoryEntity category);

    @Nonnull
    Optional<CategoryEntity> findById(UUID id);

    @Nonnull
    Optional<CategoryEntity> findByUsernameAndName(String username, String categoryName);

    @Nonnull
    List<CategoryEntity> findAllByUsername(String username);

    @Nonnull
    List<CategoryEntity> findAll();

    void delete(CategoryEntity category);
}

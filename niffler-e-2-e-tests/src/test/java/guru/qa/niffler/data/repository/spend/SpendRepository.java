package guru.qa.niffler.data.repository.spend;

import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.entity.spend.SpendEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpendRepository {
    SpendEntity create(SpendEntity spend);

    Optional<SpendEntity> findById(UUID id);

    Optional<SpendEntity> findByUsernameAndDescription(String username, String name);

    List<SpendEntity> findAll();

    List<SpendEntity> findAllByUsername(String username);

    void delete(SpendEntity spend);

    CategoryEntity createCategory(CategoryEntity category);

    Optional<CategoryEntity> findCategoryById(UUID id);

    Optional<CategoryEntity> findCategoryByUsernameAndName(String username, String categoryName);

    List<CategoryEntity> findAllCategoryByUsername(String username);

    List<CategoryEntity> findAllCategory();

    CategoryEntity updateCategory(CategoryEntity category);

    void deleteCategory(CategoryEntity category);
}

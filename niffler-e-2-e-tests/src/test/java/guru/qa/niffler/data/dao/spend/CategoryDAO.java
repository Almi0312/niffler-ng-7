package guru.qa.niffler.data.dao.spend;

import guru.qa.niffler.data.entity.spend.CategoryEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryDAO {
    CategoryEntity create(CategoryEntity category);

    CategoryEntity update(CategoryEntity category);

    Optional<CategoryEntity> findById(UUID id);

    Optional<CategoryEntity> findByUsernameAndName(String username, String categoryName);

    List<CategoryEntity> findAllByUsername(String username);

    List<CategoryEntity> findAll();

    void delete(CategoryEntity category);
}

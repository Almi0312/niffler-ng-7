package guru.qa.niffler.service.spend;

import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.model.SpendJson;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpendsClient {

    SpendJson createSpend(SpendJson spend);

    SpendJson updateSpend(SpendJson spend);

    CategoryJson createCategory(CategoryJson category);

    Optional<CategoryJson> findCategoryById(UUID id);

    Optional<SpendJson> findById(UUID id);

    Optional<SpendJson> findByUsernameAndDescription(String username, String name);

    List<SpendJson> findAll();

    List<SpendJson> findAllByUsername(String username);

    void remove(SpendJson spend);

    Optional<CategoryJson> findCategoryByUsernameAndName(String username, String categoryName);

    List<CategoryJson> findAllCategoryByUsername(String username);

    List<CategoryJson> findAllCategory();

    CategoryJson updateCategory(CategoryJson category);

    void removeCategory(CategoryJson category);

}

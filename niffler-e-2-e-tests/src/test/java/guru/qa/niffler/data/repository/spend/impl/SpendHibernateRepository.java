package guru.qa.niffler.data.repository.spend.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.entity.spend.SpendEntity;
import guru.qa.niffler.data.jpa.EntityManagers;
import guru.qa.niffler.data.repository.spend.SpendRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
public class SpendHibernateRepository implements SpendRepository {

    private static final Config CFG = Config.getInstance();

    private final EntityManager entityManager = EntityManagers.em(CFG.authJdbcUrl());

    @Override
    public @Nonnull SpendEntity createSpend(SpendEntity spend) {
        entityManager.joinTransaction();
        entityManager.persist(spend);
        return spend;
    }

    @Override
    public @Nonnull SpendEntity updateSpend(SpendEntity spend) {
        entityManager.joinTransaction();
        entityManager.merge(spend);
        return spend;
    }

    @Override
    public @Nonnull Optional<SpendEntity> findById(UUID id) {
        String query = "SELECT s FROM SpendEntity s JOIN FETCH s.category c WHERE s.id=:id";
        try {
            return Optional.ofNullable(entityManager.createQuery(query, SpendEntity.class)
                    .setParameter("id", id)
                    .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public @Nonnull Optional<SpendEntity> findByUsernameAndDescription(String username, String name) {
        String query = "SELECT s FROM SpendEntity s JOIN FETCH s.category c WHERE s.username=:username and s.name=:name";
        try {
            return Optional.ofNullable(entityManager.createQuery(query, SpendEntity.class)
                    .setParameter("username", username)
                    .setParameter("name", name)
                    .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public @Nonnull List<SpendEntity> findAll() {
        String query = "SELECT s FROM SpendEntity s JOIN FETCH s.category";
            return entityManager.createQuery(query, SpendEntity.class).getResultList();
    }

    @Override
    public @Nonnull List<SpendEntity> findAllByUsername(String username) {
        String query = "SELECT s FROM SpendEntity s JOIN FETCH s.category WHERE s.username=:username";
            return entityManager.createQuery(query, SpendEntity.class)
                    .setParameter("username", username)
                    .getResultList();
    }

    @Override
    public void remove(SpendEntity spend) {
        entityManager.joinTransaction();
        SpendEntity spendPersis = entityManager.find(SpendEntity.class, spend.getId());
        CategoryEntity categoryPersis = entityManager.find(CategoryEntity.class, spend.getCategory().getId());
        entityManager.remove(spendPersis);
        entityManager.remove(categoryPersis);
    }

    @Override
    public @Nonnull CategoryEntity createCategory(CategoryEntity category) {
        entityManager.joinTransaction();
        entityManager.persist(category);
        return category;
    }

    @Override
    public @Nonnull Optional<CategoryEntity> findCategoryById(UUID id) {
        return Optional.ofNullable(entityManager.find(CategoryEntity.class, id));
    }

    @Override
    public @Nonnull Optional<CategoryEntity> findCategoryByUsernameAndName(String username, String categoryName) {
        String query = "SELECT c FROM CategoryEntity c WHERE c.username=:username AND c.name=:name";
        try {
            return Optional.ofNullable(entityManager.createQuery(query, CategoryEntity.class)
                    .setParameter("username", username)
                    .setParameter("name", categoryName)
                    .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public @Nonnull List<CategoryEntity> findAllCategoryByUsername(String username) {
        String query = "SELECT c FROM CategoryEntity c WHERE c.username=:username";
            return entityManager.createQuery(query, CategoryEntity.class)
                    .setParameter("username", username)
                    .getResultList();
    }

    @Override
    public @Nonnull List<CategoryEntity> findAllCategory() {
        String query = "SELECT c FROM CategoryEntity c";
            return entityManager.createQuery(query, CategoryEntity.class)
                    .getResultList();
    }

    @Override
    public @Nonnull CategoryEntity updateCategory(CategoryEntity category) {
        entityManager.joinTransaction();
        entityManager.merge(category);
        return category;
    }

    @Override
    public void removeCategory(CategoryEntity category) {
        entityManager.joinTransaction();
        CategoryEntity categoryPersis = entityManager.find(CategoryEntity.class, category.getId());
        entityManager.remove(categoryPersis);
    }
}

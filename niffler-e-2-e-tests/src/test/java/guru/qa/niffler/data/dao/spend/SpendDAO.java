package guru.qa.niffler.data.dao.spend;

import guru.qa.niffler.data.entity.spend.SpendEntity;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
public interface SpendDAO {
    @Nonnull
    SpendEntity create(SpendEntity spend);

    @Nonnull
    SpendEntity update(SpendEntity spend);

    @Nonnull
    Optional<SpendEntity> findById(UUID id);

    @Nonnull
    Optional<SpendEntity> findByUsernameAndDescription(String username, String name);

    @Nonnull
    List<SpendEntity> findAll();

    @Nonnull
    List<SpendEntity> findAllByUsername(String username);

    void delete(SpendEntity spend);
}

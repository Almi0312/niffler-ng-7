package guru.qa.niffler.data.repository.spend.mapper;

import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.entity.spend.SpendEntity;
import guru.qa.niffler.model.CurrencyValues;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SpendEntityExtractor implements ResultSetExtractor<SpendEntity> {

    public static final SpendEntityExtractor instance = new SpendEntityExtractor();

    private SpendEntityExtractor() {
    }

    @Override
    public SpendEntity extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<UUID, SpendEntity> spendMap = new ConcurrentHashMap<>();
        UUID spendId = null;
        while (rs.next()) {
            spendId = rs.getObject("id", UUID.class);
            spendMap.computeIfAbsent(spendId, id -> {
                SpendEntity spendEntity = new SpendEntity();
                try {
                    spendEntity.setId(rs.getObject("id", UUID.class));
                    spendEntity.setUsername(rs.getString("username"));
                    spendEntity.setSpendDate(new Date(rs.getDate("spend_date").getTime()));
                    spendEntity.setCurrency(CurrencyValues.valueOf(rs.getString("currency")));
                    spendEntity.setAmount(rs.getDouble("amount"));
                    spendEntity.setDescription(rs.getString("description"));
                    CategoryEntity entity = new CategoryEntity();
                    entity.setId(rs.getObject("category_id", UUID.class));
                    if (entity.getId() != null) {
                        entity.setUsername(rs.getString("username"));
                        entity.setName(rs.getString("name"));
                        entity.setArchived(rs.getBoolean("archived"));
                    }
                    spendEntity.setCategory(entity);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                return spendEntity;
            });
        }
        if (spendMap.isEmpty()) {
            return null;
        }
        return spendMap.get(spendId);
    }
}

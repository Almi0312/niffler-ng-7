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
                    spendEntity.setCategory(new CategoryEntity());
                    spendEntity.getCategory().setId(rs.getObject("category_id", UUID.class));
                    if (spendEntity.getCategory().getId() != null) {
                        spendEntity.getCategory().setUsername(rs.getString("username"));
                        spendEntity.getCategory().setName(rs.getString("name"));
                        spendEntity.getCategory().setArchived(rs.getBoolean("archived"));
                    }
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

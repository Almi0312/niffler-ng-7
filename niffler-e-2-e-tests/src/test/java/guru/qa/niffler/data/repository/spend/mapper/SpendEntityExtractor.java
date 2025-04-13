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
        SpendEntity spend = null;
        if (rs.next()) {
            spend = new SpendEntity();
            spend.setId(rs.getObject("id", UUID.class));
            spend.setUsername(rs.getString("username"));
            spend.setSpendDate(new Date(rs.getDate("spend_date").getTime()));
            spend.setCurrency(CurrencyValues.valueOf(rs.getString("currency")));
            spend.setAmount(rs.getDouble("amount"));
            spend.setDescription(rs.getString("description"));
            CategoryEntity entity = new CategoryEntity();
            entity.setId(rs.getObject("category_id", UUID.class));
            if (entity.getId() != null) {
                entity.setUsername(rs.getString("username"));
                entity.setName(rs.getString("name"));
                entity.setArchived(rs.getBoolean("archived"));
            }
            spend.setCategory(entity);
        }
        return spend;
    }
}

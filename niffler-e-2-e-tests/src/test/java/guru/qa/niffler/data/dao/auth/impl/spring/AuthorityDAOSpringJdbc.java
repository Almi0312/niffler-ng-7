package guru.qa.niffler.data.dao.auth.impl.spring;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.auth.AuthorityDAO;
import guru.qa.niffler.data.dao.auth.mapper.AuthorityEntityRowMapper;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static guru.qa.niffler.data.template.DataSources.dataSource;

public class AuthorityDAOSpringJdbc implements AuthorityDAO {

    private static final Config CFG = Config.getInstance();

    private final JdbcTemplate jdbcTemplate;

    public AuthorityDAOSpringJdbc() {
        jdbcTemplate = new JdbcTemplate(dataSource(CFG.authJdbcUrl()));
    }

    @Override
    public void create(AuthorityEntity... authority) {
        jdbcTemplate.batchUpdate(
                "INSERT INTO authority (user_id, authority) VALUES (? , ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setObject(1, authority[i].getUserId());
                        ps.setString(2, authority[i].getAuthority().name());
                    }

                    @Override
                    public int getBatchSize() {
                        return authority.length;
                    }
                }
        );
    }

    @Override
    public List<AuthorityEntity> findByAuthUserId(AuthUserEntity authUser) {
        String query = "SELECT * FROM authority WHERE \"user_id\" = ?";
        try {
            return jdbcTemplate.query(query, AuthorityEntityRowMapper.instance, authUser.getId());
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public void delete(AuthUserEntity authUser) {
        String query = "DELETE FROM authority WHERE \"user_id\" = ?";
        jdbcTemplate.update(query, authUser.getId());
    }
}

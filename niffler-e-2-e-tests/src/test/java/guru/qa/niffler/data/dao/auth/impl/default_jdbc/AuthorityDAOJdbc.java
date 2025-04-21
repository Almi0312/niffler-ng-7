package guru.qa.niffler.data.dao.auth.impl.default_jdbc;


import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.auth.AuthorityDAO;
import guru.qa.niffler.data.dao.auth.mapper.AuthorityEntityRowMapper;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static guru.qa.niffler.data.jdbc.Connections.holder;

@ParametersAreNonnullByDefault
public class AuthorityDAOJdbc implements AuthorityDAO {

    private static final Config CFG = Config.getInstance();

    private final String authUrlJdbc = CFG.authJdbcUrl();

    @Override
    @SuppressWarnings("resource")
    public void create(AuthorityEntity... authority) {
        String query = "INSERT INTO \"authority\" (user_id, authority) VALUES (?, ?)";
        try (PreparedStatement ps = holder(authUrlJdbc).connection()
                .prepareStatement(
                query)) {
            for (AuthorityEntity a : authority) {
                ps.setObject(1, a.getUser().getId());
                ps.setString(2, a.getAuthority().name());
                ps.addBatch();
                ps.clearParameters();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @SuppressWarnings("resource")
    @Nonnull
    public List<AuthorityEntity> findByAuthUserId(AuthUserEntity authUser) {
        String query = "SELECT * FROM authority WHERE user_id = ?";
        try(PreparedStatement preparedStatement = holder(authUrlJdbc).connection()
                .prepareStatement(
                        query
                )) {
            preparedStatement.setObject(1, authUser.getId());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<AuthorityEntity> authorityEntities = new ArrayList<>();
                while (resultSet.next()) {
                    authorityEntities.add(AuthorityEntityRowMapper.instance.mapRow(resultSet, 0));
                }
                return authorityEntities;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @SuppressWarnings("resource")
    public void delete(AuthUserEntity authUser) {
        String query = "DELETE FROM authority WHERE \"user_id\" = ?";
        try (PreparedStatement preparedStatement = holder(authUrlJdbc).connection()
                .prepareStatement(
                query
        )) {
            preparedStatement.setObject(1, authUser.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

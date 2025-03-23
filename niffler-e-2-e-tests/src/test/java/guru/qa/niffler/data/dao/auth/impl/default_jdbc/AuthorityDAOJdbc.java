package guru.qa.niffler.data.dao.auth.impl.default_jdbc;


import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.auth.AuthorityDAO;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.Authority;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static guru.qa.niffler.data.template.Connections.holder;

public class AuthorityDAOJdbc implements AuthorityDAO {


    private static final Config CFG = Config.getInstance();

    @Override
    public void create(AuthorityEntity... authority) {
        String query = "INSERT INTO \"authority\" (user_id, authority) VALUES (?, ?)";
        try (PreparedStatement ps = holder(CFG.authJdbcUrl()).connection()
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
    public List<AuthorityEntity> findByAuthUserId(AuthUserEntity authUser) {
        String query = "SELECT * FROM authority WHERE \"user_id\" = ?";
        try(PreparedStatement preparedStatement = holder(CFG.authJdbcUrl()).connection()
                .prepareStatement(
                        query
                )) {
            preparedStatement.setObject(1, authUser.getId());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<AuthorityEntity> authorityEntities = new ArrayList<>();
                while (resultSet.next()) {
                    AuthorityEntity authority = new AuthorityEntity();
                    authority.setId(resultSet.getObject("id", UUID.class));
                    authority.getUser().setId(resultSet.getObject("user_id", UUID.class));
                    authority.setAuthority(Authority.valueOf(resultSet.getString("authority")));
                    authorityEntities.add(authority);
                }
                return authorityEntities;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(AuthUserEntity authUser) {
        String query = "DELETE FROM authority WHERE \"user_id\" = ?";
        try (PreparedStatement preparedStatement = holder(CFG.authJdbcUrl()).connection()
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

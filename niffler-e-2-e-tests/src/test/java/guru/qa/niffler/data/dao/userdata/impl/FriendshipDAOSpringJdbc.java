package guru.qa.niffler.data.dao.userdata.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.userdata.FriendshipDAO;
import guru.qa.niffler.data.dao.userdata.mapper.FriendshipEntityRowMapper;
import guru.qa.niffler.data.entity.userdata.FriendshipEntity;
import guru.qa.niffler.data.entity.userdata.UserdataUserEntity;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static guru.qa.niffler.data.template.DataSources.dataSource;

public class FriendshipDAOSpringJdbc implements FriendshipDAO {

    private final Config CFG = Config.getInstance();

    private final JdbcTemplate jdbcTemplate;

    public FriendshipDAOSpringJdbc() {
        jdbcTemplate = new JdbcTemplate(dataSource(CFG.userdataJdbcUrl()));
    }

    @Override
    public void create(List<FriendshipEntity> friends) {
        String createFriendshipQuery = "INSERT INTO friendship (requester_id, addressee_id, status, created_date) " +
                "VALUES (?, ?, ?, ?) ON CONFLICT (requester_id, addressee_id) DO UPDATE SET status = ?";
        jdbcTemplate.batchUpdate(createFriendshipQuery,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setObject(1, friends.get(i).getRequester().getId());
                        ps.setObject(2, friends.get(i).getAddressee().getId());
                        ps.setString(3, friends.get(i).getStatus().name());
                        ps.setDate(4, new Date(friends.get(i).getCreatedDate().getTime()));
                        ps.setString(5, friends.get(i).getStatus().name());
                    }

                    @Override
                    public int getBatchSize() {
                        return friends.size();
                    }
                });
    }

    @Override
    public List<FriendshipEntity> findUserFriendships(UserdataUserEntity user, boolean isRequester) {
        String queryRequest = "SELECT * FROM friendship f WHERE %s = ?"
                .formatted(isRequester ? "requester_id" : "addressee_id");
        return jdbcTemplate.query(queryRequest, FriendshipEntityRowMapper.instance, user.getId());
    }

    @Override
    public void delete(UserdataUserEntity userEntity) {
        String queryDeleteFriendship = "DELETE FROM friendship WHERE requester_id = ? OR addressee_id = ?";
        UUID id = userEntity.getId();
        jdbcTemplate.update(queryDeleteFriendship, id, id);
    }
}

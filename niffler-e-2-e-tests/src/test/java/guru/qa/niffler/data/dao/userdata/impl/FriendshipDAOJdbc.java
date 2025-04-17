package guru.qa.niffler.data.dao.userdata.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.userdata.FriendshipDAO;
import guru.qa.niffler.data.dao.userdata.mapper.FriendshipEntityRowMapper;
import guru.qa.niffler.data.entity.userdata.FriendshipEntity;
import guru.qa.niffler.data.entity.userdata.UserdataUserEntity;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static guru.qa.niffler.data.template.Connections.holder;

public class FriendshipDAOJdbc implements FriendshipDAO {

    private final Config CFG = Config.getInstance();
    String udJdbcUrl = CFG.userdataJdbcUrl();

    @Override
    public void create(List<FriendshipEntity> friends) {
        String createFriendshipQuery = "INSERT INTO friendship (requester_id, addressee_id, status, created_date) " +
                "VALUES (?, ?, ?, ?) ON CONFLICT (requester_id, addressee_id) DO UPDATE SET status = ?";
        try (PreparedStatement createFriendshipPs = holder(udJdbcUrl).connection()
                .prepareStatement(createFriendshipQuery)) {
            for (FriendshipEntity friend : friends) {
                createFriendshipPs.setObject(1, friend.getRequester().getId());
                createFriendshipPs.setObject(2, friend.getAddressee().getId());
                createFriendshipPs.setString(3, friend.getStatus().name());
                createFriendshipPs.setDate(4, new Date(friend.getCreatedDate().getTime()));
                createFriendshipPs.setString(5, friend.getStatus().name());
                createFriendshipPs.addBatch();
                createFriendshipPs.clearParameters();
            }
            createFriendshipPs.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<FriendshipEntity> findUserFriendships(UserdataUserEntity user, boolean isRequester) {
        String query = "SELECT * FROM friendship f WHERE %s = ?"
                .formatted(isRequester ? "requester_id" : "addressee_id");
        try (PreparedStatement ps = holder(udJdbcUrl).connection()
                .prepareStatement(query)) {
            ps.setObject(1, user.getId());
            try (ResultSet resultSet = ps.executeQuery()) {
                List<FriendshipEntity> friendshipEntities = new ArrayList<>();
                while (resultSet.next()) {
                    friendshipEntities.add(FriendshipEntityRowMapper.instance.mapRow(resultSet, 0));
                }
                return friendshipEntities;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(UserdataUserEntity userEntity) {
        String queryDeleteFriendship = "DELETE FROM friendship WHERE requester_id = ? OR addressee_id = ?";
        UUID id = userEntity.getId();
        try (PreparedStatement deleteFriendshipPs = holder(udJdbcUrl).connection()
                .prepareStatement(queryDeleteFriendship)) {
            deleteFriendshipPs.setObject(1, id);
            deleteFriendshipPs.setObject(2, id);
            deleteFriendshipPs.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

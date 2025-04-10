package guru.qa.niffler.data.repository.userdata.mapper.spring;

import guru.qa.niffler.data.entity.userdata.FriendshipEntity;
import guru.qa.niffler.data.entity.userdata.FriendshipStatus;
import guru.qa.niffler.data.entity.userdata.UserdataUserEntity;
import guru.qa.niffler.model.CurrencyValues;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UdUserEntityExtractor implements ResultSetExtractor<UserdataUserEntity> {

    public static final UdUserEntityExtractor instance = new UdUserEntityExtractor();

    private UdUserEntityExtractor() {
    }

    @Override
    public UserdataUserEntity extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<UUID, UserdataUserEntity> usersMap = new ConcurrentHashMap<>();
        UUID userId = null;
        while (rs.next()) {
            userId = rs.getObject("id", UUID.class);
            UserdataUserEntity user = usersMap.computeIfAbsent(userId, id -> {
                UserdataUserEntity result = new UserdataUserEntity();
                try {
                    result.setId(id);
                    result.setUsername(rs.getString("username"));
                    result.setCurrency(CurrencyValues.valueOf(rs.getString("currency")));
                    result.setFirstname(rs.getString("firstname"));
                    result.setSurname(rs.getString("surname"));
                    result.setFullname(rs.getString("full_name"));
                    result.setPhoto(rs.getBytes("photo"));
                    result.setPhotoSmall(rs.getBytes("photo_small"));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                return result;
            });
            UUID addresseeId = rs.getObject("addressee_id", UUID.class);
            UUID requesterId = rs.getObject("requester_id", UUID.class);
            if (addresseeId != null && requesterId != null) {
                FriendshipEntity friend = new FriendshipEntity();
                friend.getAddressee().setId(addresseeId);
                friend.getRequester().setId(requesterId);
                friend.setStatus(FriendshipStatus.valueOf(rs.getString("status")));
                friend.setCreatedDate(new Date(rs.getDate("created_date").getTime()));
                if (friend.getAddressee().getId().equals(user.getId())) {
                    user.getFriendshipAddressees().add(friend);
                } else {
                    user.getFriendshipRequests().add(friend);
                }
            }
        }
        if(usersMap.isEmpty()) {
            return null;
        }
        return usersMap.get(userId);
    }
}


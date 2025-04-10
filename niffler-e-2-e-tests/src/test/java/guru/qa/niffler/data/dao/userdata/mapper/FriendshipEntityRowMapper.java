package guru.qa.niffler.data.dao.userdata.mapper;

import guru.qa.niffler.data.entity.userdata.FriendshipEntity;
import guru.qa.niffler.data.entity.userdata.FriendshipStatus;
import guru.qa.niffler.data.entity.userdata.UserdataUserEntity;
import guru.qa.niffler.model.CurrencyValues;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class FriendshipEntityRowMapper implements RowMapper<FriendshipEntity> {

    public static final FriendshipEntityRowMapper instance = new FriendshipEntityRowMapper();

    private FriendshipEntityRowMapper() {
    }

    @Override
    public FriendshipEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        FriendshipEntity friendship = new FriendshipEntity();
        friendship.setRequester(new UserdataUserEntity());
        friendship.setAddressee(new UserdataUserEntity());
        friendship.getRequester().setId(rs.getObject("requester_id", UUID.class));
        friendship.getAddressee().setId(rs.getObject("addressee_id", UUID.class));
        friendship.setStatus(FriendshipStatus.valueOf(rs.getString("status")));
        friendship.setCreatedDate(new Date(rs.getDate("created_date").getTime()));
        return friendship;
    }
}


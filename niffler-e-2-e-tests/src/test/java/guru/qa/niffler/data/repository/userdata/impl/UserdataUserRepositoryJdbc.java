package guru.qa.niffler.data.repository.userdata.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.userdata.FriendshipDAO;
import guru.qa.niffler.data.dao.userdata.UserdataUserDAO;
import guru.qa.niffler.data.dao.userdata.impl.FriendshipDAOJdbc;
import guru.qa.niffler.data.dao.userdata.impl.UserdataUserDAOJdbc;
import guru.qa.niffler.data.entity.userdata.FriendshipEntity;
import guru.qa.niffler.data.entity.userdata.FriendshipStatus;
import guru.qa.niffler.data.entity.userdata.UserdataUserEntity;
import guru.qa.niffler.data.repository.userdata.UserdataUserRepository;
import guru.qa.niffler.data.repository.userdata.mapper.UdUserEntityExtractor;

import java.sql.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static guru.qa.niffler.data.template.Connections.holder;

public class UserdataUserRepositoryJdbc implements UserdataUserRepository {
    private static final Config CFG = Config.getInstance();

    private final String udJdbcUrl = CFG.userdataJdbcUrl();
    private final FriendshipDAO friendshipDAO = new FriendshipDAOJdbc();
    private final UserdataUserDAO userdataUserDAO = new UserdataUserDAOJdbc();

    @Override
    public UserdataUserEntity create(UserdataUserEntity user) {
        return userdataUserDAO.create(user);
    }

    @Override
    public Optional<UserdataUserEntity> findById(UUID id) {
        return userdataUserDAO.findById(id);
    }

    public Optional<UserdataUserEntity> findByIdWithFriendship(UUID id) {
        String query = "SELECT * FROM \"user\" u LEFT JOIN friendship f" +
                " ON (u.id = f.requester_id OR u.id = f.addressee_id) WHERE u.id = ?";
        try (PreparedStatement ps = holder(udJdbcUrl).connection().prepareStatement(query)) {
            ps.setObject(1, id);
            try (ResultSet resultSet = ps.executeQuery()) {
                UserdataUserEntity user = UdUserEntityExtractor.instance.extractData(resultSet);
                if (user != null) {
                    return Optional.of(user);
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<UserdataUserEntity> findByUsername(String username) {
        return userdataUserDAO.findByUsername(username);
    }

    public Optional<UserdataUserEntity> findByUsernameWithFriendship(String username) {
        String query = "SELECT * FROM \"user\" u LEFT JOIN friendship f" +
                " ON (u.id = f.requester_id OR u.id = f.addressee_id) WHERE u.username = ?";
        try (PreparedStatement ps = holder(udJdbcUrl).connection().prepareStatement(query)) {
            ps.setString(1, username);
            try (ResultSet resultSet = ps.executeQuery()) {
                UserdataUserEntity user = UdUserEntityExtractor.instance.extractData(resultSet);
                if (user != null) {
                    return Optional.of(user);
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public UserdataUserEntity update(UserdataUserEntity user) {
        return userdataUserDAO.update(user);
    }

    @Override
    public void delete(UserdataUserEntity userdataUserEntity) {
        friendshipDAO.delete(userdataUserEntity);
        userdataUserDAO.delete(userdataUserEntity);
    }

    @Override
    public List<FriendshipEntity> findUserFriendships(UserdataUserEntity user, boolean isRequester) {
        return friendshipDAO.findUserFriendships(user, isRequester);
    }

    @Override
    public void createRequester(FriendshipStatus status, UserdataUserEntity requester, UserdataUserEntity... addressees) {
        requester.addFriends(status, addressees);
        friendshipDAO.create(requester.getFriendshipRequests());
    }

    @Override
    public void createAddressee(UserdataUserEntity requester, UserdataUserEntity... requesters) {
        requester.addInvitations(requesters);
        friendshipDAO.create(requester.getFriendshipAddressees());
    }
}

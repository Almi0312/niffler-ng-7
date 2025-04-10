package guru.qa.niffler.data.repository.userdata.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.userdata.FriendshipDAO;
import guru.qa.niffler.data.dao.userdata.UserdataUserDAO;
import guru.qa.niffler.data.dao.userdata.impl.FriendshipDAOSpringJdbc;
import guru.qa.niffler.data.dao.userdata.impl.UserdataUserDAOSpringJdbc;
import guru.qa.niffler.data.entity.userdata.FriendshipEntity;
import guru.qa.niffler.data.entity.userdata.FriendshipStatus;
import guru.qa.niffler.data.entity.userdata.UserdataUserEntity;
import guru.qa.niffler.data.repository.userdata.UserdataUserRepository;
import guru.qa.niffler.data.repository.userdata.mapper.UdUserEntityExtractor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static guru.qa.niffler.data.template.DataSources.dataSource;

public class UserdataUserSpringRepositoryJdbc implements UserdataUserRepository {

    private static final Config CFG = Config.getInstance();

    private final FriendshipDAO friendshipDAO;
    private final UserdataUserDAO userdataUserDAO;
    private final JdbcTemplate jdbcTemplate;

    public UserdataUserSpringRepositoryJdbc() {
        jdbcTemplate = new JdbcTemplate(dataSource(CFG.authJdbcUrl()));
        friendshipDAO = new FriendshipDAOSpringJdbc();
        userdataUserDAO = new UserdataUserDAOSpringJdbc();
    }

    @Override
    public UserdataUserEntity create(UserdataUserEntity user) {
        return userdataUserDAO.create(user);
    }

    @Override
    public Optional<UserdataUserEntity> findById(UUID id) {
        return userdataUserDAO.findById(id);
    }

    @Override
    public Optional<UserdataUserEntity> findByIdWithFriendship(UUID id) {
        Optional<UserdataUserEntity> user = userdataUserDAO.findById(id);
        if (user.isEmpty()) {
            return Optional.empty();
        }
        user.get().getFriendshipRequests().addAll(friendshipDAO.findUserFriendships(user.get(), true));
        user.get().getFriendshipAddressees().addAll(friendshipDAO.findUserFriendships(user.get(), false));
        return user;
    }

    @Override
    public Optional<UserdataUserEntity> findByUsername(String username) {
        return userdataUserDAO.findByUsername(username);
    }

    @Override
    public Optional<UserdataUserEntity> findByUsernameWithFriendship(String username) {
        String query = """
                SELECT *
                FROM "user" u
                LEFT JOIN friendship f ON (u.id = f.requester_id OR u.id = f.addressee_id)
                WHERE u.username = ?
                """;
        try {
            return Optional.ofNullable(jdbcTemplate.query(query, UdUserEntityExtractor.instance, username));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
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

    public void createRequester(FriendshipStatus status, UserdataUserEntity requester, UserdataUserEntity... addressees) {
        requester.addFriends(status, addressees);
        friendshipDAO.create(requester.getFriendshipRequests());
    }

    public void createAddressee(UserdataUserEntity request, UserdataUserEntity... requesters) {
        request.addInvitations(requesters);
        friendshipDAO.create(request.getFriendshipAddressees());
    }

}

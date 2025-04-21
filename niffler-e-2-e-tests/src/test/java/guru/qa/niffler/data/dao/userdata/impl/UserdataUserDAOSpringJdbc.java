package guru.qa.niffler.data.dao.userdata.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.userdata.UserdataUserDAO;
import guru.qa.niffler.data.dao.userdata.mapper.UdUserEntityRowMapper;
import guru.qa.niffler.data.entity.userdata.UserdataUserEntity;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.sql.*;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static guru.qa.niffler.data.jdbc.DataSources.dataSource;

@ParametersAreNonnullByDefault
public class UserdataUserDAOSpringJdbc implements UserdataUserDAO {

    private static final Config CFG = Config.getInstance();

    private final JdbcTemplate jdbcTemplate;

    public UserdataUserDAOSpringJdbc() {
        jdbcTemplate = new JdbcTemplate(dataSource(CFG.userdataJdbcUrl()));
    }

    @Nonnull
    @Override
    public UserdataUserEntity create(UserdataUserEntity user) {
        String query = "INSERT INTO \"user\" (username, currency, firstname, surname, photo, photo_small, full_name) " +
                "VALUES (?,?,?,?,?,?,?)";
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    query, Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getCurrency().name());
            ps.setString(3, user.getFirstname());
            ps.setString(4, user.getSurname());
            ps.setBytes(5, user.getPhoto());
            ps.setBytes(6, user.getPhotoSmall());
            ps.setString(7, user.getFullname());
            return ps;
        }, kh);

        final UUID generatedKey = (UUID) kh.getKeys().get("id");
        user.setId(generatedKey);
        return user;
    }

    @Nonnull
    @Override
    public Optional<UserdataUserEntity> findById(UUID id) {
        String query = "SELECT * FROM \"user\" WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    query, UdUserEntityRowMapper.instance, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Nonnull
    @Override
    public Optional<UserdataUserEntity> findByUsername(String username) {
        String query = "SELECT * FROM \"user\" WHERE username = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    query, UdUserEntityRowMapper.instance, username));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Nonnull
    @Override
    public List<UserdataUserEntity> findAll() {
        String query = "SELECT * FROM \"user\" WHERE username = ?";
        try {
            return jdbcTemplate.query(
                    query, UdUserEntityRowMapper.instance);
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    @Nonnull
    @Override
    public UserdataUserEntity update(UserdataUserEntity user) {
        String queryUpdateUser = "UPDATE \"user\" SET username = ?, currency = ?, firstname = ?, surname = ?, photo = ?, " +
                "photo_small = ?, full_name = ? WHERE id = ?";
        jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(
                        queryUpdateUser
                );
        ps.setString(1, user.getUsername());
        ps.setString(2, user.getCurrency().name());
        ps.setString(3, user.getFirstname());
        ps.setString(4, user.getSurname());
        ps.setBytes(5, user.getPhoto());
        ps.setBytes(6, user.getPhotoSmall());
        ps.setString(7, user.getFullname());
        ps.setObject(8, user.getId());
        return ps;
    });
        return user;
    }

    @Override
    public void delete(UserdataUserEntity userdataUserEntity) {
        String query = "DELETE FROM \"user\" WHERE id = ?";
        jdbcTemplate.update(query, userdataUserEntity.getId());
    }
}

package guru.qa.niffler.data.dao.auth.impl.spring;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.auth.AuthUserDAO;
import guru.qa.niffler.data.dao.auth.mapper.AuthUserEntityRowMapper;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.*;
import java.util.Optional;
import java.util.UUID;

import static guru.qa.niffler.data.template.DataSources.dataSource;

public class AuthUserDAOSpringJdbc implements AuthUserDAO {

    private static final Config CFG = Config.getInstance();

    private final JdbcTemplate jdbcTemplate;

    public AuthUserDAOSpringJdbc() {
        jdbcTemplate = new JdbcTemplate(dataSource(CFG.authJdbcUrl()));
    }

    @Override
    public AuthUserEntity create(AuthUserEntity user) {
        String query = "INSERT INTO \"user\" (username, password, enabled, account_non_expired, account_non_locked, credentials_non_expired) " +
                "VALUES (?,?,?,?,?,?)";
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    query, Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setBoolean(3, user.getEnabled());
            ps.setBoolean(4, user.getAccountNonExpired());
            ps.setBoolean(5, user.getAccountNonLocked());
            ps.setBoolean(6, user.getCredentialsNonExpired());
            return ps;
        }, kh);

        final UUID generatedKey = (UUID) kh.getKeys().get("id");
        user.setId(generatedKey);
        return user;
    }

    @Override
    public Optional<AuthUserEntity> findById(UUID id) {
        String query = "SELECT * FROM \"user\" WHERE id = ?";
        return Optional.ofNullable(
                jdbcTemplate.queryForObject(
                        query, AuthUserEntityRowMapper.instance, id)
        );
    }

    @Override
    public Optional<AuthUserEntity> findByUsername(String userEntity) {
        String query = "SELECT * FROM \"user\" WHERE username = ?";
        return Optional.ofNullable(
                jdbcTemplate.queryForObject(
                        query, AuthUserEntityRowMapper.instance, userEntity)
        );
    }

    @Override
    public void deleteByUsername(AuthUserEntity authUserEntity) {
        String query = "DELETE FROM \"user\" WHERE username = ?";
        jdbcTemplate.update(query, authUserEntity.getId());
    }
}
package guru.qa.niffler.data.repository.auth.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.repository.auth.AuthUserRepository;
import guru.qa.niffler.data.repository.auth.mapper.AuthUserEntityRowExtractor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.UUID;

import static guru.qa.niffler.data.template.DataSources.dataSource;

public class AuthUserSpringRepositoryJdbc implements AuthUserRepository {

    private static final Config CFG = Config.getInstance();

    private final JdbcTemplate jdbcTemplate;

    public AuthUserSpringRepositoryJdbc() {
        jdbcTemplate = new JdbcTemplate(dataSource(CFG.authJdbcUrl()));
    }

    @Override
    public AuthUserEntity create(AuthUserEntity user) {
        String createUserQuery = "INSERT INTO \"user\" (username, password, enabled, account_non_expired, account_non_locked, credentials_non_expired) " +
                "VALUES (?,?,?,?,?,?)";
        String authorityCreateQuery = "INSERT INTO authority (user_id, authority) VALUES (?, ?)";
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    createUserQuery, Statement.RETURN_GENERATED_KEYS
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
        jdbcTemplate.batchUpdate(authorityCreateQuery, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setObject(1, generatedKey);
                ps.setString(2, user.getAuthorities().get(i).getAuthority().name());
            }

            @Override
            public int getBatchSize() {
                return user.getAuthorities().size();
            }
        });
        return user;
    }

    @Override
    public Optional<AuthUserEntity> findById(UUID id) {
        String query = "SELECT * FROM \"user\" u JOIN authority a ON (u.id = a.user_id) WHERE u.id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.query(
                    query, AuthUserEntityRowExtractor.instance, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<AuthUserEntity> findByUsername(String userEntity) {
        String query = "SELECT u.id, u.username, u.password, u.enabled, u.account_non_expired," +
                " u.account_non_locked, u.credentials_non_expired, a.id AS authority_id, a.user_id, a.authority" +
                " FROM \"user\" AS u JOIN authority AS a ON (u.id = a.user_id) WHERE u.username = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.query(
                    query, AuthUserEntityRowExtractor.instance, userEntity));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteById(AuthUserEntity authUserEntity) {
        String deleteUserQuery = "DELETE FROM \"user\" WHERE id = ?";
        String deleteAuthorityQuery = "DELETE FROM authority WHERE user_id = ?";
        UUID id = authUserEntity.getId();
        jdbcTemplate.update(deleteAuthorityQuery, id);
        jdbcTemplate.update(deleteUserQuery, id);
    }
}
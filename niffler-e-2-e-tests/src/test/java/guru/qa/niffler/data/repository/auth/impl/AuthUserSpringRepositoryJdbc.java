package guru.qa.niffler.data.repository.auth.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.auth.AuthUserDAO;
import guru.qa.niffler.data.dao.auth.AuthorityDAO;
import guru.qa.niffler.data.dao.auth.impl.spring.AuthUserDAOSpringJdbc;
import guru.qa.niffler.data.dao.auth.impl.spring.AuthorityDAOSpringJdbc;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import guru.qa.niffler.data.repository.auth.AuthUserRepository;
import guru.qa.niffler.data.repository.auth.mapper.AuthUserEntityRowExtractor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;
import java.util.UUID;

import static guru.qa.niffler.data.template.DataSources.dataSource;

public class AuthUserSpringRepositoryJdbc implements AuthUserRepository {

    private static final Config CFG = Config.getInstance();

    private final JdbcTemplate jdbcTemplate;
    private final AuthUserDAO authUserDAO;
    private final AuthorityDAO authorityDAO;

    public AuthUserSpringRepositoryJdbc() {
        jdbcTemplate = new JdbcTemplate(dataSource(CFG.authJdbcUrl()));
        authUserDAO = new AuthUserDAOSpringJdbc();
        authorityDAO = new AuthorityDAOSpringJdbc();
    }

    @Override
    public AuthUserEntity create(AuthUserEntity user) {
        user.setId(authUserDAO.create(user).getId());
        authorityDAO.create(user.getAuthorities().toArray(AuthorityEntity[]::new));
        return user;
    }

    @Override
    public AuthUserEntity update(AuthUserEntity authUserEntity) {
        return authUserDAO.update(authUserEntity);
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
    public void remove(AuthUserEntity authUserEntity) {
        authorityDAO.delete(authUserEntity);
        authUserDAO.delete(authUserEntity);
    }
}
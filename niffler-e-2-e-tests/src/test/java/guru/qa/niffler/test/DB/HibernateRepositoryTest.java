package guru.qa.niffler.test.DB;

import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.userdata.FriendshipStatus;
import guru.qa.niffler.data.entity.userdata.UserdataUserEntity;
import guru.qa.niffler.data.repository.userdata.UserdataRepository;
import guru.qa.niffler.data.repository.userdata.impl.UserdataHibernateRepository;
import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.model.SpendJson;
import guru.qa.niffler.model.UserdataUserJson;
import guru.qa.niffler.service.auth.AuthUserHibernateDBClient;
import guru.qa.niffler.service.SpendsClient;
import guru.qa.niffler.service.spend.SpendHibernateDBClient;
import guru.qa.niffler.service.userdata.UserdataHibernateDBClient;
import guru.qa.niffler.service.UsersClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static guru.qa.niffler.config.Constants.MAIN_USERNAME;
import static guru.qa.niffler.model.CurrencyValues.RUB;

@Slf4j
class HibernateRepositoryTest {

    static UsersClient userDBRepo = new UserdataHibernateDBClient();
    static SpendsClient dbSpend = new SpendHibernateDBClient();
    static AuthUserHibernateDBClient authUserDBClient = new AuthUserHibernateDBClient();

    @CsvSource(value =
            "hibernate testFriend1, hibernate requesterNatali, 12345")
    @ParameterizedTest
    void checkRequestFriendship(String testUserName, String otherUserName, String password) {
        UserdataRepository userRepository = new UserdataHibernateRepository();

        log.info("Создание объекта {}", testUserName);
        UserdataUserJson testUserJson = userDBRepo.findByUsername(testUserName).orElseGet(() ->
                userDBRepo.create(testUserName, RUB, password));

        userRepository.findById(testUserJson.id()).orElseThrow(() ->
                new RuntimeException("объект с username = %s не был создан".formatted(testUserName)));

        log.info("Отправка запроса в друзья от {} для {}", otherUserName, testUserName);
        userDBRepo.createIncomeInvitations(testUserJson, otherUserName); // requester
        UserdataUserEntity otherUserEntity = userRepository.findByUsername(otherUserName).orElseThrow(
                () -> new RuntimeException("объект с username = %s не был создан".formatted(otherUserName)));

        log.info("ПРОВЕРКИ КОРРЕКТНОСТИ СОЗДАННОЙ ДРУЖБЫ\n" +
                "Отправлен запрос в друзья от {} для {}", otherUserName, testUserName);
        Assertions.assertEquals(otherUserEntity.getFriendshipRequests().getFirst().getAddressee().getId(), testUserJson.id());
        Assertions.assertEquals(otherUserEntity.getFriendshipRequests().getFirst().getRequester().getId(), otherUserEntity.getId());
        Assertions.assertEquals(FriendshipStatus.PENDING, otherUserEntity.getFriendshipRequests().getFirst().getStatus());
        Assertions.assertTrue(otherUserEntity.getFriendshipAddressees().isEmpty());

        log.info("Удаление пользователя {}", otherUserEntity);
        userDBRepo.remove(UserdataUserJson.fromEntity(otherUserEntity, null));
        log.info("Удаление пользователя {}", testUserName);
        userDBRepo.remove(testUserJson);

        log.info("Проверка что пользователи удалены");
        Assertions.assertNull(authUserDBClient.findUserByUsername(
                otherUserName).orElse(null));
        Assertions.assertNull(authUserDBClient.findUserByUsername(
                testUserName).orElse(null));
        Assertions.assertNull(userRepository.findByUsername(
                otherUserName).orElse(null));
        Assertions.assertNull(userRepository.findByUsername(
                testUserName).orElse(null));
    }

    @CsvSource(value =
            "hibernate testFriend2, hibernate addresseeNatali, 12345")
    @ParameterizedTest
    void checkAddresseeFriendship(String testUserName, String otherUserName, String password) {
        UserdataRepository userRepository = new UserdataHibernateRepository();

        log.info("Создание объекта {}", testUserName);
        UserdataUserJson testUserJson = userDBRepo.findByUsername(testUserName).orElseGet(() ->
                userDBRepo.create(testUserName, RUB, password));

        userRepository.findById(testUserJson.id()).orElseThrow(() ->
                new RuntimeException("объект с username = %s не был создан".formatted(testUserName)));

        log.info("Отправка запроса в друзья от {} для {}", testUserName, otherUserName);
        userDBRepo.createOutcomeInvitations(testUserJson, otherUserName); // addressee
        UserdataUserEntity otherUserEntity = userRepository.findByUsername(otherUserName).orElseThrow();

        log.info("ПРОВЕРКИ КОРРЕКТНОСТИ СОЗДАННОЙ ДРУЖБЫ\n" +
                "Отправлен запрос в друзья от {} для {}", testUserName, otherUserName);
        Assertions.assertEquals(otherUserEntity.getFriendshipAddressees().getFirst().getAddressee().getId(), otherUserEntity.getId());
        Assertions.assertEquals(otherUserEntity.getFriendshipAddressees().getFirst().getRequester().getId(), testUserJson.id());
        Assertions.assertEquals(FriendshipStatus.PENDING, otherUserEntity.getFriendshipAddressees().getFirst().getStatus());
        Assertions.assertTrue(otherUserEntity.getFriendshipRequests().isEmpty());

        log.info("Удаление пользователя");
        userDBRepo.remove(testUserJson);
        userDBRepo.remove(UserdataUserJson.fromEntity(otherUserEntity, null));

        log.info("Проверка что пользователи удалены");
        Assertions.assertNull(authUserDBClient.findUserByUsername(
                otherUserName).orElse(null));
        Assertions.assertNull(authUserDBClient.findUserByUsername(
                testUserName).orElse(null));
        Assertions.assertNull(userRepository.findByUsername(
                otherUserName).orElse(null));
        Assertions.assertNull(userRepository.findByUsername(
                testUserName).orElse(null));
    }

    @CsvSource(value =
            "hibernate testFriend3, hibernate friendNatali, 12345")
    @ParameterizedTest
    void checkFriendFriendship(String testUserName, String otherUserName, String password) {
        UserdataRepository userRepository = new UserdataHibernateRepository();

        log.info("Создание объекта {}", testUserName);
        UserdataUserJson testUserJson = userDBRepo.findByUsername(testUserName).orElseGet(() ->
                userDBRepo.create(testUserName, RUB, password));
        UserdataUserEntity testUserEntity = userRepository.findById(testUserJson.id()).orElseThrow();

        log.info("{} и {} должны быть друзьями", testUserName, otherUserName);
        userDBRepo.createFriends(testUserJson, otherUserName); // friend
        UserdataUserEntity otherUserEntity = userRepository.findByUsername(otherUserName).orElseThrow();

        log.info("ПРОВЕРКИ КОРРЕКТНОСТИ СОЗДАННОЙ ДРУЖБЫ\n" +
                "Стали друзьями");
        Assertions.assertEquals(otherUserEntity.getFriendshipAddressees().getFirst().getAddressee().getId(), otherUserEntity.getId());
        Assertions.assertEquals(otherUserEntity.getFriendshipAddressees().getFirst().getRequester().getId(), testUserJson.id());
        Assertions.assertEquals(1, otherUserEntity.getFriendshipRequests().size());
        Assertions.assertEquals(1, otherUserEntity.getFriendshipAddressees().size());
        Assertions.assertEquals(1, testUserEntity.getFriendshipRequests().size());
        Assertions.assertEquals(1, testUserEntity.getFriendshipAddressees().size());
        Assertions.assertEquals(FriendshipStatus.ACCEPTED, otherUserEntity.getFriendshipRequests().getFirst().getStatus());
        Assertions.assertEquals(FriendshipStatus.ACCEPTED, otherUserEntity.getFriendshipAddressees().getFirst().getStatus());
        Assertions.assertEquals(FriendshipStatus.ACCEPTED, testUserEntity.getFriendshipRequests().getFirst().getStatus());
        Assertions.assertEquals(FriendshipStatus.ACCEPTED, testUserEntity.getFriendshipAddressees().getFirst().getStatus());

        log.info("Удаление пользователя {}", testUserEntity);
        userDBRepo.remove(testUserJson);
        log.info("Удаление пользователя {}", otherUserEntity);
        userDBRepo.remove(UserdataUserJson.fromEntity(otherUserEntity, null));

        log.info("Проверка что пользователи удалены");
        Assertions.assertNull(authUserDBClient.findUserByUsername(
                otherUserName).orElse(null));
        Assertions.assertNull(authUserDBClient.findUserByUsername(
                testUserName).orElse(null));
        Assertions.assertNull(userDBRepo.findByUsername(
                testUserName).orElse(null));
        Assertions.assertNull(userDBRepo.findByUsername(
                otherUserName).orElse(null));
    }

    @CsvSource(value =
            "hibernate top cource, hibernate cool cource")
    @ParameterizedTest
    void daoCRUDSpendTest(String categoryName, String spendDescription) {
        dbSpend.findByUsernameAndDescription(MAIN_USERNAME, spendDescription)
                .ifPresent(entity -> {
                    dbSpend.remove(entity);
                    log.info("SPEND DELETED");
                });
        SpendJson newSpendJson = dbSpend.createSpend(new SpendJson(
                null,
                Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()),
                new CategoryJson(null,
                        categoryName,
                        MAIN_USERNAME,
                        true),
                RUB,
                100.0,
                spendDescription,
                MAIN_USERNAME
        ));
        log.info("SPEND CREATED");
        log.info("CATEGORY CREATED");
        Assertions.assertNotNull(newSpendJson);
        Assertions.assertNotNull(newSpendJson.category());
        SpendJson resultSpend = dbSpend.findByUsernameAndDescription(MAIN_USERNAME, spendDescription).orElse(null);
        Assertions.assertNotNull(resultSpend);
        Assertions.assertNotNull(resultSpend.category());
        Assertions.assertEquals(newSpendJson, resultSpend,
                "объект %s не равен %s".formatted(newSpendJson, resultSpend));
    }

    @Test
    void daoCreateAndCheckUserTest() {
        UserdataHibernateRepository userdataRepository = new UserdataHibernateRepository();
        String username = "hibernate twix";
        String password = "12345";
        userdataRepository.findByUsername(username)
                .ifPresent(userJson -> {
                    userDBRepo.remove(UserdataUserJson.fromEntity(userJson, null));
                    log.info("DELETED USER");
                });

        log.info("CREATE USER");
        UserdataUserJson createUserJson = userDBRepo
                .create(username, RUB, password);

        log.info("CHECK USER");
        UserdataUserJson findedUserJson = userdataRepository.findById(createUserJson.id())
                .map(user -> UserdataUserJson.fromEntity(user, null)).orElse(null);
        Assertions.assertNotNull(findedUserJson);
        Assertions.assertEquals(createUserJson, findedUserJson);

        findedUserJson = userdataRepository.findById(createUserJson.id())
                .map(user -> UserdataUserJson.fromEntity(user, null)).orElse(null);
        Assertions.assertNotNull(findedUserJson);
        Assertions.assertEquals(createUserJson, findedUserJson);

        log.info("CHECK AUTH_USER");
        AuthUserEntity authUserEntity = authUserDBClient.findUserByUsername(findedUserJson.username()).orElse(null);
        Assertions.assertNotNull(authUserEntity);
        Assertions.assertEquals(2, authUserEntity.getAuthorities().size());
        Assertions.assertEquals(findedUserJson.username(), authUserEntity.getUsername());

        log.info("DELETE USER");
        userDBRepo.remove(createUserJson);
        authUserEntity = authUserDBClient.findUserByUsername(createUserJson.username()).orElse(null);
        Assertions.assertNull(authUserEntity);
        findedUserJson = userDBRepo.findById(createUserJson.id()).orElse(null);
        Assertions.assertNull(findedUserJson);
    }

    @Test
    void checkTransactionSpendTest() {
        String categoryName = "hibernate top cource transac2";
        String spendDescription = "hibernate cool cource transac";
        dbSpend.findByUsernameAndDescription(MAIN_USERNAME, spendDescription)
                .ifPresent(entity -> {
                    dbSpend.remove(entity);
                    log.info("SPEND DELETED");
                });
        SpendJson spendJson = new SpendJson(null,
                new Date(),
                new CategoryJson(null,
                        categoryName,
                        MAIN_USERNAME,
                        true),
                null, // падение транзакции
                100.0,
                spendDescription,
                MAIN_USERNAME);
        Assertions.assertThrows(RuntimeException.class,
                () -> dbSpend.createSpend(spendJson));
        log.info("CHECK NOT SPEND CREATED");
        SpendJson resultSpend = dbSpend
                .findByUsernameAndDescription(MAIN_USERNAME, spendDescription).orElse(null);
        Assertions.assertNull(resultSpend,
                "объект %s был создан(".formatted(resultSpend));
        log.info("CHECK NOT CATEGORY CREATED");
        CategoryJson categoryJson = dbSpend
                .findCategoryByUsernameAndName(MAIN_USERNAME, categoryName).orElse(null);
        Assertions.assertNull(categoryJson,
                "объект %s был создан(".formatted(categoryJson));
    }

    @Test
    void checkTransactionByUserTest() {
        UserdataHibernateRepository userRepository = new UserdataHibernateRepository();
        String username = "hibernate usertransac";
        String password = "12345";
        userRepository.findByUsername(username)
                .ifPresent(userJson -> {
                    userDBRepo.remove(UserdataUserJson.fromEntity(userJson, null));
                    log.info("DELETED USER");
                });
        Assertions.assertThrows(RuntimeException.class, () ->
                userDBRepo.create(username,
                        null, // Здесь падение транзакции
                        password));
        log.info("EXPECT NOT CREATED USER");
        UserdataUserJson udUser = userRepository.findByUsername(username)
                .map(user -> UserdataUserJson.fromEntity(user, null)).orElse(null);
        Assertions.assertNull(udUser, "Юзер %s был создан".formatted(udUser));
        log.info("EXPECT NOT CREATED AUTH_USER");
        AuthUserEntity authUserEntity = authUserDBClient.findUserByUsername(username).orElse(null);
        Assertions.assertNull(authUserEntity, "Юзер авторизации %s был создан".formatted(authUserEntity));
    }
}

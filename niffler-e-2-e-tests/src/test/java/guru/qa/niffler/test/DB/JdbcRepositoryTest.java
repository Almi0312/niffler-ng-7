package guru.qa.niffler.test.DB;

import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.userdata.*;
import guru.qa.niffler.data.entity.userdata.FriendshipStatus;
import guru.qa.niffler.data.repository.userdata.UserdataRepository;
import guru.qa.niffler.data.repository.userdata.impl.UserdataRepositoryJdbc;
import guru.qa.niffler.model.*;
import guru.qa.niffler.service.auth.AuthUserDBRepositoryClient;
import guru.qa.niffler.service.spend.SpendDBRepositoryClient;
import guru.qa.niffler.service.SpendsClient;
import guru.qa.niffler.service.userdata.UserdataApiClient;
import guru.qa.niffler.service.userdata.UserdataDBRepositoryClient;
import guru.qa.niffler.service.UsersClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

import static guru.qa.niffler.config.Constants.MAIN_USERNAME;
import static guru.qa.niffler.model.CurrencyValues.*;

@Slf4j
class JdbcRepositoryTest {

    @CsvSource(value =
            {"jdbc testFriend1, jdbc requesterNatali, 12345"})
    @ParameterizedTest
    void checkRequestFriendship(String testUserName, String otherUserName, String password) {
        UsersClient userDBRepo = new UserdataDBRepositoryClient();
        AuthUserDBRepositoryClient authUserDBClient = new AuthUserDBRepositoryClient();
        UserdataRepository userRepository = new UserdataRepositoryJdbc();
        log.info("Создание объекта {}", testUserName);
        UserdataUserJson testUserJson = userDBRepo.findByUsername(testUserName).orElseGet(() ->
                userDBRepo.create(testUserName, RUB, password));

        log.info("проверка создания {}", testUserName);
        testUserJson = userDBRepo.findById(
                UserdataUserEntity.fromJson(testUserJson).getId()).orElseThrow(() ->
                new RuntimeException("Объект с username - %s не создан".formatted(testUserName)));

        log.info("Отправка запроса в друзья от {} для {}", otherUserName, testUserName);
        userDBRepo.createIncomeInvitations(testUserJson, otherUserName); // requester

        log.info("Получение созданного юзера {} и его дружеские связи", otherUserName);
        UserdataUserEntity otherUserEntity = userRepository.findByUsernameWithFriendship(otherUserName)
                        .orElseThrow(() -> new RuntimeException("Объект с username - %s не создан".formatted(otherUserName)));

        log.info("ПРОВЕРКИ КОРРЕКТНОСТИ СОЗДАННОЙ ДРУЖБЫ\n" +
                "Отправлен запрос в друзья от {} для {}", otherUserEntity, testUserJson);
        Assertions.assertEquals(otherUserEntity.getFriendshipRequests().getFirst().getAddressee().getId(), testUserJson.id());
        Assertions.assertEquals(otherUserEntity.getFriendshipRequests().getFirst().getRequester().getId(), otherUserEntity.getId());
        Assertions.assertEquals(FriendshipStatus.PENDING, otherUserEntity.getFriendshipRequests().getFirst().getStatus());
        Assertions.assertTrue(otherUserEntity.getFriendshipAddressees().isEmpty());

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
            {"jdbc testFriend2, jdbc addresseeNatali, 12345"})
    @ParameterizedTest
    void checkAddresseeFriendship(String testUserName, String otherUserName, String password) {
        UsersClient userDBRepo = new UserdataDBRepositoryClient();
        AuthUserDBRepositoryClient authUserDBClient = new AuthUserDBRepositoryClient();
        UserdataRepository userRepository = new UserdataRepositoryJdbc();

        log.info("Создание объекта {}", testUserName);
        UserdataUserJson testUserJson = userDBRepo.findByUsername(testUserName).orElseGet(() ->
                userDBRepo.create(testUserName, RUB, password));

        log.info("проверка создания {}", testUserName);
        testUserJson = userDBRepo.findById(
                UserdataUserEntity.fromJson(testUserJson).getId()).orElseThrow(() ->
                new RuntimeException("Объект с username - %s не создан".formatted(testUserName)));
        log.info("Отправка запроса в друзья от {} для {}", testUserName, otherUserName);
        userDBRepo.createOutcomeInvitations(testUserJson, otherUserName); // addressee

        log.info("Получение созданного юзера {} и его дружеские связи", otherUserName);
        UserdataUserEntity otherUserEntity = userRepository.findByUsernameWithFriendship(otherUserName)
                .orElseThrow(() -> new RuntimeException("Объект с username - %s не создан".formatted(otherUserName)));

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
            {"jdbc testFriend3, jdbc friendNatali, 12345"})
    @ParameterizedTest
    void checkFriendFriendship(String testUserName, String otherUserName, String password) {
        UsersClient userDBRepo = new UserdataDBRepositoryClient();
        AuthUserDBRepositoryClient authUserDBClient = new AuthUserDBRepositoryClient();
        UserdataRepository userRepository = new UserdataRepositoryJdbc();

        log.info("Создание объекта {}", testUserName);
        UserdataUserJson testUserJson = userDBRepo.findByUsername(testUserName).orElseGet(() ->
                userDBRepo.create(testUserName, RUB, password));

        log.info("проверка создания {}", testUserName);
        testUserJson = userDBRepo.findById(
                UserdataUserEntity.fromJson(testUserJson).getId()).orElseThrow(() ->
                new RuntimeException("Объект с username - %s не создан".formatted(testUserName)));
        log.info("{} и {} должны быть друзьями", testUserName, otherUserName);
        userDBRepo.createFriends(testUserJson, otherUserName); // friend

        log.info("Получение созданного юзера {} и его дружеские связи", otherUserName);
        UserdataUserEntity otherUserEntity = userRepository.findByUsernameWithFriendship(otherUserName)
                .orElseThrow(() -> new RuntimeException("Объект с username - %s не создан".formatted(otherUserName)));
        UserdataUserEntity testUserEntity = userRepository.findByUsernameWithFriendship(testUserName)
                .orElseThrow(() -> new RuntimeException("Объект с username - %s не создан".formatted(testUserName)));

        log.info("ПРОВЕРКИ КОРРЕКТНОСТИ СОЗДАННОЙ ДРУЖБЫ\n" +
                "Отправлен запрос в друзья от {} для {}", testUserName, otherUserName);
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
            {"jdbc top cource, jdbc cool cource"})
    @ParameterizedTest
    void daoCRUDSpendTest(String categoryName, String spendDescription) {
        SpendsClient dbSpend = new SpendDBRepositoryClient();
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

    @CsvSource(value =
            {"jdbc twix, 12345"})
    @ParameterizedTest
    void daoCreateAndCheckUserTest(String username, String password) {
        UsersClient userdataDBClient = new UserdataDBRepositoryClient();
        AuthUserDBRepositoryClient authUserDBClient = new AuthUserDBRepositoryClient();
        userdataDBClient.findByUsername(username)
                .ifPresent(userJson -> {
                    userdataDBClient.remove(userJson);
                    log.info("DELETED USER");
                });

        log.info("CREATE USER");
        UserdataUserJson createUserJson = userdataDBClient.create(
                username, RUB, password);

        log.info("CHECK USER");
        UserdataUserJson findedUserJson = userdataDBClient.findById(createUserJson.id()).orElse(null);
        Assertions.assertNotNull(findedUserJson);
        Assertions.assertEquals(createUserJson, findedUserJson);

        findedUserJson = userdataDBClient.findByUsername(username).orElse(null);
        Assertions.assertNotNull(findedUserJson);
        Assertions.assertEquals(createUserJson, findedUserJson);

        log.info("CHECK AUTH_USER");
        AuthUserEntity authUserEntity = authUserDBClient.findUserByUsername(findedUserJson.username()).orElse(null);
        Assertions.assertNotNull(authUserEntity);
        Assertions.assertEquals(2, authUserEntity.getAuthorities().size());
        Assertions.assertEquals(findedUserJson.username(), authUserEntity.getUsername());

        log.info("DELETE USER");
        userdataDBClient.remove(createUserJson);
        findedUserJson = userdataDBClient.findById(createUserJson.id()).orElse(null);
        Assertions.assertNull(findedUserJson);
        authUserEntity = authUserDBClient.findUserByUsername(createUserJson.username()).orElse(null);
        Assertions.assertNull(authUserEntity);
    }

    @CsvSource(value =
            {"jdbc top cource transac2, jdbc cool cource transac2"})
    @ParameterizedTest
    void checkTransactionSpendTest(String categoryName, String spendDescription) {
        SpendsClient dbSpend = new SpendDBRepositoryClient();
        dbSpend.findByUsernameAndDescription(MAIN_USERNAME, spendDescription)
                .ifPresent(entity -> {
                    dbSpend.remove(entity);
                    log.info("SPEND DELETED");
                });
        SpendJson spendJson = new SpendJson(
                null,
                new Date(),
                new CategoryJson(null,
                        categoryName,
                        MAIN_USERNAME,
                        true),
                null, // падение транзакции
                100.0,
                spendDescription,
                MAIN_USERNAME
        );
        Assertions.assertThrows(RuntimeException.class,
                () -> dbSpend.createSpend(spendJson));
        log.info("CHECK NOT SPEND CREATED");
        SpendJson resultSpend = dbSpend
                .findByUsernameAndDescription(MAIN_USERNAME, spendDescription).orElse(null);
        Assertions.assertNull(resultSpend);
        log.info("CHECK NOT CATEGORY CREATED");
        CategoryJson categoryEntity = dbSpend
                .findCategoryByUsernameAndName(MAIN_USERNAME, categoryName).orElse(null);
        Assertions.assertNull(categoryEntity);
    }

    @CsvSource(value =
            {"jdbc transac, 12345"})
    @ParameterizedTest
    void checkTransactionByUserTest(String username, String password) {
        UsersClient userdataDBClient = new UserdataDBRepositoryClient();
        AuthUserDBRepositoryClient authUserDBClient = new AuthUserDBRepositoryClient();
        userdataDBClient.findByUsername(username)
                .ifPresent(userJson -> {
                    userdataDBClient.remove(userJson);
                    log.info("DELETED USER");
                });
        Assertions.assertThrows(RuntimeException.class, () ->
                userdataDBClient.create(
                        username, null, // падение транзакции
                        password));

        log.info("EXPECT NOT CREATED USER");
        UserdataUserJson udUser = userdataDBClient.findByUsername(username).orElse(null);
        Assertions.assertNull(udUser);

        log.info("EXPECT NOT CREATED AUTH_USER");
        AuthUserEntity authUserEntity = authUserDBClient.findUserByUsername(username).orElse(null);
        Assertions.assertNull(authUserEntity);
    }
}
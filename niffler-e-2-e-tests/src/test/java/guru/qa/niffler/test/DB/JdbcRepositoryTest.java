package guru.qa.niffler.test.DB;

import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.userdata.*;
import guru.qa.niffler.data.repository.userdata.UserdataUserRepository;
import guru.qa.niffler.data.repository.userdata.impl.UserdataRepositoryJdbc;
import guru.qa.niffler.model.*;
import guru.qa.niffler.service.auth.repository.AuthUserDBRepositoryClient;
import guru.qa.niffler.service.spend.dao.CategoryDBClient;
import guru.qa.niffler.service.spend.repository.SpendDBRepositoryClient;
import guru.qa.niffler.service.userdata.repository.UserdataDBRepositoryClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static guru.qa.niffler.config.Constants.MAIN_USERNAME;
import static guru.qa.niffler.model.CurrencyValues.*;
import static guru.qa.niffler.util.RandomDataUtils.*;

@Slf4j
class JdbcRepositoryTest {

    @Test
    void checkRequestFriendship() {
        UserdataDBRepositoryClient userDBRepo = new UserdataDBRepositoryClient();
        UserdataUserRepository userRepository = new UserdataRepositoryJdbc();
        String testUserName = "testFriend1";
        String otherUserName = "requesterNatali";

        log.info("Создание объекта {}", testUserName);
        UserdataUserJson testUser = userDBRepo.findByUsername(testUserName).orElseGet(() ->
                userDBRepo.create(new UserdataUserJson(
                        null, testUserName, null, null,
                        null, RUB, null, null, null)));

        log.info("проверка создания {}", testUser.username());
        UserdataUserEntity testUserEntity = userRepository.findById(
                UserdataUserEntity.fromJson(testUser).getId()).orElse(null);
        Assertions.assertNotNull(testUserEntity, "объект %s не был создан".formatted(testUserEntity));

        log.info("Создание объекта для отправки запроса в друзья {}", otherUserName);
        UserdataUserJson otherUser = userDBRepo.findByUsername(otherUserName).orElseGet(() ->
                userDBRepo.create(new UserdataUserJson(
                        null, otherUserName, null, null,
                        null, USD, null, null, null)));

        log.info("проверка создания {}", otherUser.username());
        UserdataUserEntity otherUserEntity = userRepository.findById(
                UserdataUserEntity.fromJson(otherUser).getId()).orElse(null);
        Assertions.assertNotNull(otherUserEntity, "объект %s не был создан".formatted(otherUserEntity));

        log.info("Отправка запроса в друзья от {} для {}", otherUserEntity, testUserEntity);
        userDBRepo.createIncomeInvitations(testUserEntity, otherUserEntity); // requester
        otherUserEntity.getFriendshipRequests().addAll(userRepository.findUserFriendships(otherUserEntity, true));

        log.info("ПРОВЕРКИ КОРРЕКТНОСТИ СОЗДАННОЙ ДРУЖБЫ\n" +
                "Отправлен запрос в друзья от {} для {}", otherUserEntity, testUserEntity);
        Assertions.assertEquals(otherUserEntity.getFriendshipRequests().getFirst().getAddressee(), testUserEntity);
        Assertions.assertEquals(otherUserEntity.getFriendshipRequests().getFirst().getRequester(), otherUserEntity);
        Assertions.assertEquals(FriendshipStatus.PENDING, otherUserEntity.getFriendshipRequests().getFirst().getStatus());
        Assertions.assertTrue(otherUserEntity.getFriendshipAddressees().isEmpty());

        log.info("Удаление пользователя");
        userDBRepo.delete(testUser);
        userDBRepo.delete(otherUser);

        log.info("Проверка что пользователи удалены");
        Assertions.assertNull(userRepository.findByIdWithFriendship(
                UserdataUserEntity.fromJson(testUser).getId()).orElse(null));
        Assertions.assertNull(userRepository.findByIdWithFriendship(
                UserdataUserEntity.fromJson(otherUser).getId()).orElse(null));
    }

    @Test
    void checkAddresseeFriendship() {
        UserdataDBRepositoryClient userDBRepo = new UserdataDBRepositoryClient();
        UserdataUserRepository userRepository = new UserdataRepositoryJdbc();
        String testUserName = "testFriend2";
        String otherUserName = "addresseeNatali";

        log.info("Создание объекта {}", testUserName);
        UserdataUserJson testUser = userDBRepo.findByUsername(testUserName).orElseGet(() ->
                userDBRepo.create(new UserdataUserJson(
                        null, testUserName, null, null,
                        null, RUB, null, null, null)));

        log.info("проверка создания {}", testUser.username());
        UserdataUserEntity testUserEntity = userRepository.findById(
                UserdataUserEntity.fromJson(testUser).getId()).orElse(null);
        Assertions.assertNotNull(testUserEntity, "объект %s не был создан".formatted(testUserEntity));

        log.info("Создание объекта для отправки запроса в друзья {}", otherUserName);
        UserdataUserJson otherUser = userDBRepo.findByUsername(otherUserName).orElseGet(() ->
                userDBRepo.create(new UserdataUserJson(
                        null, otherUserName, null, null,
                        null, USD, null, null, null)));

        log.info("проверка создания {}", otherUser.username());
        UserdataUserEntity otherUserEntity = userRepository.findById(
                UserdataUserEntity.fromJson(otherUser).getId()).orElse(null);
        Assertions.assertNotNull(otherUserEntity, "объект %s не был создан".formatted(otherUserEntity));

        log.info("Отправка запроса в друзья");
        userDBRepo.createOutcomeInvitations(testUserEntity, otherUserEntity); // addressee
        testUserEntity.getFriendshipRequests().addAll(userRepository.findUserFriendships(testUserEntity, true));

        log.info("ПРОВЕРКИ КОРРЕКТНОСТИ СОЗДАННОЙ ДРУЖБЫ\n" +
                "Отправлен запрос в друзья от {} для {}", testUserEntity, otherUserEntity);
        Assertions.assertEquals(testUserEntity.getFriendshipRequests().getFirst().getAddressee(), otherUserEntity);
        Assertions.assertEquals(testUserEntity.getFriendshipRequests().getFirst().getRequester(), testUserEntity);
        Assertions.assertEquals(FriendshipStatus.PENDING, testUserEntity.getFriendshipRequests().getFirst().getStatus());
        Assertions.assertTrue(testUserEntity.getFriendshipAddressees().isEmpty());

        log.info("Удаление пользователя");
        userDBRepo.delete(testUser);
        userDBRepo.delete(otherUser);

        log.info("Проверка что пользователи удалены");
        Assertions.assertNull(userRepository.findByIdWithFriendship(
                UserdataUserEntity.fromJson(testUser).getId()).orElse(null));
        Assertions.assertNull(userRepository.findByIdWithFriendship(
                UserdataUserEntity.fromJson(otherUser).getId()).orElse(null));
    }

    @Test
    void checkFriendFriendship() {
        UserdataDBRepositoryClient userDBRepo = new UserdataDBRepositoryClient();
        UserdataUserRepository userRepository = new UserdataRepositoryJdbc();
        String testUserName = "testFriend3";
        String otherUserName = "friendNatali";

        log.info("Создание объекта {}", testUserName);
        UserdataUserJson testUser = userDBRepo.findByUsername(testUserName).orElseGet(() ->
                userDBRepo.create(new UserdataUserJson(
                        null, testUserName, null, null,
                        null, RUB, null, null, null)));

        log.info("проверка создания {}", testUser.username());
        UserdataUserEntity testUserEntity = userRepository.findById(
                UserdataUserEntity.fromJson(testUser).getId()).orElse(null);
        Assertions.assertNotNull(testUserEntity, "объект %s не был создан".formatted(testUserEntity));

        log.info("Создание объекта для отправки запроса в друзья {}", otherUserName);
        UserdataUserJson otherUser = userDBRepo.findByUsername(otherUserName).orElseGet(() ->
                userDBRepo.create(new UserdataUserJson(
                        null, otherUserName, null, null,
                        null, USD, null, null, null)));

        log.info("проверка создания {}", otherUser.username());
        UserdataUserEntity otherUserEntity = userRepository.findById(
                UserdataUserEntity.fromJson(otherUser).getId()).orElse(null);
        Assertions.assertNotNull(otherUserEntity, "объект %s не был создан".formatted(otherUserEntity));

        log.info("Отправка запроса в друзья");
        userDBRepo.createFriends(testUserEntity, otherUserEntity); // friend
        otherUserEntity.getFriendshipRequests().addAll(userRepository.findUserFriendships(otherUserEntity, true));

        log.info("ПРОВЕРКИ КОРРЕКТНОСТИ СОЗДАННОЙ ДРУЖБЫ\n" +
                "Стали друзьями");
        Assertions.assertEquals(FriendshipStatus.ACCEPTED, testUserEntity.getFriendshipRequests().getFirst().getStatus());
        Assertions.assertEquals(FriendshipStatus.ACCEPTED, otherUserEntity.getFriendshipRequests().getFirst().getStatus());

        log.info("Удаление пользователя");
        userDBRepo.delete(testUser);
        userDBRepo.delete(otherUser);

        log.info("Проверка что пользователи удалены");
        Assertions.assertNull(userRepository.findByIdWithFriendship(
                UserdataUserEntity.fromJson(testUser).getId()).orElse(null));
        Assertions.assertNull(userRepository.findByIdWithFriendship(
                UserdataUserEntity.fromJson(otherUser).getId()).orElse(null));
    }

    @Test
    void daoCRUDSpendTest() {
        String categoryName = "top cource";
        String spendDescription = "cool cource";
        SpendDBRepositoryClient dbSpend = new SpendDBRepositoryClient();
        dbSpend.findByUsernameAndDescription(MAIN_USERNAME, spendDescription)
                .ifPresent(entity -> {
                    dbSpend.delete(entity);
                    log.info("SPEND DELETED");
                });
        SpendJson newSpendJson = dbSpend.create(new SpendJson(
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
        UserdataDBRepositoryClient userdataDBClient = new UserdataDBRepositoryClient();
        AuthUserDBRepositoryClient authUserDBClient = new AuthUserDBRepositoryClient();
        String username = "twix";
        userdataDBClient.findByUsername(username)
                .ifPresent(userJson -> {
                    userdataDBClient.delete(userJson);
                    log.info("DELETED USER");
                });

        log.info("CREATE USER");
        UserdataUserJson createUserJson = userdataDBClient.create(
                new UserdataUserJson(
                        null,
                        username,
                        randomSurname(),
                        randomSurname(),
                        randomName(),
                        RUB,
                        null,
                        null,
                        null));

        log.info("CHECK USER");
        UserdataUserJson findedUserJson = userdataDBClient.findById(createUserJson.id()).orElse(null);
        Assertions.assertNotNull(findedUserJson);
        Assertions.assertEquals(createUserJson, findedUserJson);

        findedUserJson = userdataDBClient.findByUsername(username).orElse(null);
        Assertions.assertNotNull(findedUserJson);
        Assertions.assertEquals(createUserJson, findedUserJson);

        log.info("CHECK AUTH_USER");
        AuthUserEntity authUserEntity = authUserDBClient.findUserByUsername(findedUserJson.username()).orElse(null);
        Assertions.assertNotNull(authUserEntity, "Юзер авторизации %s не был создан".formatted(authUserEntity));
        Assertions.assertEquals(2, authUserEntity.getAuthorities().size(), "Authority не был создан корректно");
        Assertions.assertEquals(findedUserJson.username(), authUserEntity.getUsername(), "Юзер создан неверно");

        log.info("DELETE USER");
        userdataDBClient.delete(createUserJson);
        findedUserJson = userdataDBClient.findById(createUserJson.id()).orElse(null);
        Assertions.assertNull(findedUserJson, "Юзер %s не удален".formatted(findedUserJson));
        authUserEntity = authUserDBClient.findUserByUsername(createUserJson.username()).orElse(null);
        Assertions.assertNull(authUserEntity, "Юзер авторизации %s был создан".formatted(authUserEntity));
    }

    @Test
    void checkTransactionSpendTest() {
        String categoryName = "top cource transac2";
        String spendDescription = "cool cource transac";
        SpendDBRepositoryClient dbSpend = new SpendDBRepositoryClient();
        CategoryDBClient categoryDbClient = new CategoryDBClient();
        dbSpend.findByUsernameAndDescription(MAIN_USERNAME, spendDescription)
                .ifPresent(entity -> {
                    dbSpend.delete(entity);
                    log.info("SPEND DELETED");
                });
        Assertions.assertThrows(RuntimeException.class,
                () -> dbSpend.create(new SpendJson(
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
                )));
        log.info("CHECK NOT SPEND CREATED");
        SpendJson resultSpend = dbSpend
                .findByUsernameAndDescription(MAIN_USERNAME, spendDescription).orElse(null);
        Assertions.assertNull(resultSpend,
                "объект %s был создан(".formatted(resultSpend));
        log.info("CHECK NOT CATEGORY CREATED");
        CategoryJson categoryJson = categoryDbClient
                .findByUsernameAndName(MAIN_USERNAME, categoryName).orElse(null);
        Assertions.assertNull(categoryJson,
                "объект %s был создан(".formatted(categoryJson));
    }

    @Test
    void checkTransactionByUserTest() {
        UserdataDBRepositoryClient userdataDBClient = new UserdataDBRepositoryClient();
        AuthUserDBRepositoryClient authUserDBClient = new AuthUserDBRepositoryClient();
        String username = "transac";
        userdataDBClient.findByUsername(username)
                .ifPresent(userJson -> {
                    userdataDBClient.delete(userJson);
                    log.info("DELETED USER");
                });
        Assertions.assertThrows(RuntimeException.class, () ->
                userdataDBClient.create(
                        new UserdataUserJson(
                                null,
                                username,
                                randomSurname(),
                                randomSurname(),
                                randomName(),
                                null, // Здесь падение транзакции
                                null,
                                null,
                                null
                        )));
        log.info("EXPECT NOT CREATED USER");
        UserdataUserJson udUser = userdataDBClient.findByUsername(username).orElse(null);
        Assertions.assertNull(udUser, "Юзер %s был создан".formatted(udUser));
        log.info("EXPECT NOT CREATED AUTH_USER");
        AuthUserEntity authUserEntity = authUserDBClient.findUserByUsername(username).orElse(null);
        Assertions.assertNull(authUserEntity, "Юзер авторизации %s был создан".formatted(authUserEntity));
    }
}
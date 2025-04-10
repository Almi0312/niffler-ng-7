package guru.qa.niffler.test.web;

import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.userdata.FriendshipStatus;
import guru.qa.niffler.data.entity.userdata.UserdataUserEntity;
import guru.qa.niffler.data.repository.userdata.UserdataUserRepository;
import guru.qa.niffler.data.repository.userdata.impl.UserdataUserRepositoryJdbc;
import guru.qa.niffler.data.repository.userdata.impl.UserdataUserSpringRepositoryJdbc;
import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.model.SpendJson;
import guru.qa.niffler.model.UserdataUserJson;
import guru.qa.niffler.service.auth.AuthUserDBRepositoryClient;
import guru.qa.niffler.service.auth.AuthUserDBSpringRepositoryClient;
import guru.qa.niffler.service.spend.CategoryDBClient;
import guru.qa.niffler.service.spend.CategoryDBSpringClient;
import guru.qa.niffler.service.spend.SpendDBRepositoryClient;
import guru.qa.niffler.service.spend.SpendDBSpringRepositoryClient;
import guru.qa.niffler.service.userdata.UserdataDBRepositoryClient;
import guru.qa.niffler.service.userdata.UserdataDBSpringRepositoryClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static guru.qa.niffler.config.Constants.MAIN_USERNAME;
import static guru.qa.niffler.model.CurrencyValues.RUB;
import static guru.qa.niffler.model.CurrencyValues.USD;
import static guru.qa.niffler.util.RandomDataUtils.randomName;
import static guru.qa.niffler.util.RandomDataUtils.randomSurname;

@Slf4j
class JdbcSpringRepositoryTest {

    @Test
    void checkRequestFriendship() {
        UserdataDBSpringRepositoryClient userDBRepo = new UserdataDBSpringRepositoryClient();
        UserdataUserSpringRepositoryJdbc userRepository = new UserdataUserSpringRepositoryJdbc();
        String testUserName = "springFriend1";
        String otherUserName = "springRequesterNatali";

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
        UserdataUserEntity otherUserEntity = userRepository.findByIdWithFriendship(
                UserdataUserEntity.fromJson(otherUser).getId()).orElse(null);
        Assertions.assertNotNull(otherUserEntity, "объект %s не был создан".formatted(otherUserEntity));

        log.info("Отправка запроса в друзья");
        userDBRepo.createRequester(testUserEntity, otherUserEntity); // requester
        otherUserEntity.getFriendshipAddressees().addAll(userRepository.findUserFriendships(otherUserEntity, false));

        log.info("ПРОВЕРКИ КОРРЕКТНОСТИ СОЗДАННОЙ ДРУЖБЫ\n" +
                "Отправлен запрос в друзья");
        Assertions.assertEquals(testUserEntity.getFriendshipRequests().getFirst().getAddressee(), otherUserEntity);
        Assertions.assertEquals(testUserEntity.getFriendshipRequests().getFirst().getRequester(), testUserEntity);
        Assertions.assertEquals(FriendshipStatus.PENDING, testUserEntity.getFriendshipRequests().getFirst().getStatus());
        Assertions.assertEquals(otherUserEntity.getFriendshipAddressees().getFirst().getAddressee(), otherUserEntity);
        Assertions.assertEquals(otherUserEntity.getFriendshipAddressees().getFirst().getRequester(), testUserEntity);
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
        UserdataDBSpringRepositoryClient userDBRepo = new UserdataDBSpringRepositoryClient();
        UserdataUserSpringRepositoryJdbc userRepository = new UserdataUserSpringRepositoryJdbc();
        String testUserName = "springFriend2";
        String otherUserName = "springAddresseeNatali";

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
        userDBRepo.createAddressee(testUserEntity, otherUserEntity); // addressee
        otherUserEntity.getFriendshipRequests().addAll(userRepository.findUserFriendships(otherUserEntity, true));

        log.info("ПРОВЕРКИ КОРРЕКТНОСТИ СОЗДАННОЙ ДРУЖБЫ\n" +
                "Есть запрос в друзья");
        Assertions.assertEquals(testUserEntity.getFriendshipAddressees().getFirst().getAddressee(), testUserEntity);
        Assertions.assertEquals(testUserEntity.getFriendshipAddressees().getFirst().getRequester(), otherUserEntity);
        Assertions.assertEquals(FriendshipStatus.PENDING, testUserEntity.getFriendshipAddressees().getFirst().getStatus());
        Assertions.assertTrue(otherUserEntity.getFriendshipRequests().isEmpty());
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
    void checkFriendFriendship() {
        UserdataDBSpringRepositoryClient userDBRepo = new UserdataDBSpringRepositoryClient();
        UserdataUserSpringRepositoryJdbc userRepository = new UserdataUserSpringRepositoryJdbc();
        String testUserName = "springFriend3";
        String otherUserName = "springFriendNatali";

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
        Assertions.assertEquals(testUserEntity.getFriendshipRequests().getFirst().getStatus(), FriendshipStatus.ACCEPTED);
        Assertions.assertEquals(otherUserEntity.getFriendshipRequests().getFirst().getStatus(), FriendshipStatus.ACCEPTED);
        Assertions.assertEquals(otherUserEntity.getFriendshipRequests().getFirst().getRequester().getId(),
                otherUserEntity.getId());
        Assertions.assertEquals(otherUserEntity.getFriendshipRequests().getFirst().getAddressee().getId(),
                testUserEntity.getId());

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
        SpendDBSpringRepositoryClient dbSpend = new SpendDBSpringRepositoryClient();
        dbSpend.findByUsernameAndDescription(MAIN_USERNAME, spendDescription)
                .ifPresent(entity -> {
                    dbSpend.delete(entity);
                    log.info("SPEND DELETED");
                });
        SpendJson newSpendJson = dbSpend.create(new SpendJson(
                null,
                new Date(),
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
        Assertions.assertNotNull(newSpendJson, "Объект Spend не создан");
        Assertions.assertNotNull(newSpendJson.category(), "Объект Category не создан");
        SpendJson resultSpend = dbSpend.findByUsernameAndDescription(MAIN_USERNAME, spendDescription).orElse(null);
        Assertions.assertNotNull(resultSpend, "Объект Spend не создан");
        Assertions.assertNotNull(resultSpend.category(), "Объект Category не создан");
        Assertions.assertEquals(newSpendJson, resultSpend,
                "объект %s не равен %s".formatted(newSpendJson, resultSpend));
    }

    @Test
    void daoCreateAndCheckUserTest() {
        UserdataDBSpringRepositoryClient userdataDBClient = new UserdataDBSpringRepositoryClient();
        AuthUserDBSpringRepositoryClient authUserDBClient = new AuthUserDBSpringRepositoryClient();
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
        Assertions.assertNotNull(findedUserJson, "Юзер %s не создан".formatted(createUserJson));
        Assertions.assertEquals(createUserJson, findedUserJson, "объекты %s не равны %s"
                .formatted(createUserJson, findedUserJson));

        findedUserJson = userdataDBClient.findByUsername(username).orElse(null);
        Assertions.assertNotNull(findedUserJson, "Юзер %s не создан".formatted(createUserJson));
        Assertions.assertEquals(createUserJson, findedUserJson, "объекты %s не равны %s"
                .formatted(createUserJson, findedUserJson));

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
        SpendDBSpringRepositoryClient dbSpend = new SpendDBSpringRepositoryClient();
        CategoryDBSpringClient categoryDbClient = new CategoryDBSpringClient();
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
        UserdataDBSpringRepositoryClient userdataDBClient = new UserdataDBSpringRepositoryClient();
        AuthUserDBSpringRepositoryClient authUserDBClient = new AuthUserDBSpringRepositoryClient();
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
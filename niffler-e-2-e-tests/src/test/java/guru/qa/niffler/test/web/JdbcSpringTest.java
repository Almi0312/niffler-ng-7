package guru.qa.niffler.test.web;

import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.model.SpendJson;
import guru.qa.niffler.model.UserdataUserJson;
import guru.qa.niffler.service.spend.CategoryDBSpringClient;
import guru.qa.niffler.service.spend.SpendDBSpringClient;
import guru.qa.niffler.service.auth.AuthUserDBSpringClient;
import guru.qa.niffler.service.userdata.UserdataDBSpringClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static guru.qa.niffler.config.Constants.MAIN_USERNAME;
import static guru.qa.niffler.model.CurrencyValues.RUB;
import static guru.qa.niffler.util.RandomDataUtils.randomName;
import static guru.qa.niffler.util.RandomDataUtils.randomSurname;

@Slf4j
class JdbcSpringTest {

    @Test
    void daoCRUDSpringSpendTest() {
        String categoryName = "top cource";
        String spendDescription = "cool cource";
        SpendDBSpringClient dbSpendSpring = new SpendDBSpringClient();
        SpendJson spendJson = dbSpendSpring.findByUsernameAndDescription(MAIN_USERNAME, spendDescription);
        if (spendJson != null) {
            dbSpendSpring.delete(spendJson);
            log.info("SPEND DELETED");
        }
        SpendJson newSpendJson = dbSpendSpring.create(new SpendJson(
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
        Assertions.assertNotNull(newSpendJson.category(),"Объект Category не создан");
        SpendJson resultSpend = dbSpendSpring.findByUsernameAndDescription(MAIN_USERNAME, spendDescription);
        Assertions.assertTrue(newSpendJson.username().equals(resultSpend.username())
                        && newSpendJson.id().equals(resultSpend.id())
                        && newSpendJson.category().id().equals(resultSpend.category().id()),
                "Объект %s неправильно создан. В БД такой %s".formatted(newSpendJson, resultSpend));
        Assertions.assertEquals(newSpendJson.category().name(), resultSpend.category().name(),
                "%s для %s не равны".formatted(newSpendJson.category(), resultSpend.category()));
    }

    @Test
    void checkCRUDCategorySpringTest() {
        String categoryName = "Top category";
        CategoryDBSpringClient dbSpringClient = new CategoryDBSpringClient();
        dbSpringClient.findByUsernameAndName(MAIN_USERNAME, categoryName)
                .ifPresent(category -> {
                    dbSpringClient.delete(category);
                    log.info("CATEGORY DELETED");
                });
        CategoryJson newCategoryJson = dbSpringClient.create(
                new CategoryJson(
                        null,
                        categoryName,
                        MAIN_USERNAME,
                        true
                ));
        log.info("CATEGORY CREATED");
        Assertions.assertNotNull(newCategoryJson);
        Assertions.assertEquals(newCategoryJson.id(), dbSpringClient.findById(newCategoryJson.id()).get().id());
        Assertions.assertEquals(newCategoryJson.name(), dbSpringClient.findByUsernameAndName(MAIN_USERNAME, categoryName).get().name());
    }

    @Test
    void daoCreateAndCheckUserTest() {
        UserdataDBSpringClient userdataDBClient = new UserdataDBSpringClient();
        AuthUserDBSpringClient authUserDBClient = new AuthUserDBSpringClient();
        String username = "twixSpring";
        UserdataUserJson userJson = userdataDBClient.findByUsername(username);
        if (userJson != null) {
            userdataDBClient.delete(userJson);
            log.info("DELETED USER");
        }
        log.info("CREATE USER");
        UserdataUserJson userdataUserJson = userdataDBClient.create(
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
        userJson = userdataDBClient.findById(userdataUserJson.id());
        Assertions.assertNotNull(userdataUserJson, "Юзер %s не создан".formatted(userdataUserJson.toString()));
        Assertions.assertTrue(userdataUserJson.id().equals(userJson.id())
                && userdataUserJson.username().equals(userJson.username()), "Юзер %s не создан, а создан %s"
                .formatted(userJson.toString(), userdataUserJson.toString()));
        userJson = userdataDBClient.findByUsername(username);
        Assertions.assertEquals(userdataUserJson.id(), userJson.id(), "Юзер %s не создан, а создан %s"
                .formatted(userJson.toString(), userdataUserJson.toString()));
        log.info("CHECK AUTH_USER");
        AuthUserEntity authUserEntity = authUserDBClient.findUserByUsername(userJson.username());
        Assertions.assertNotNull(authUserEntity, "Юзер авторизации %s не был создан".formatted(authUserEntity));
        Assertions.assertFalse(authUserDBClient.findAuthorityByUserId(authUserEntity).isEmpty(), "Authority не был создан");
        log.info("DELETE USER");
        userdataDBClient.delete(userdataUserJson);
        userJson = userdataDBClient.findById(userdataUserJson.id());
        Assertions.assertNull(userJson, "Юзер %s не удален".formatted(userJson));
        authUserEntity = authUserDBClient.findUserByUsername(userdataUserJson.username());
        Assertions.assertNull(authUserEntity, "Юзер авторизации %s был создан".formatted(authUserEntity));
    }

    @Test
    void checkTransactionByUserTest() {
        UserdataDBSpringClient userdataDBClient = new UserdataDBSpringClient();
        AuthUserDBSpringClient authUserDBClient = new AuthUserDBSpringClient();
        String username = "transac";
        UserdataUserJson userJson = userdataDBClient.findByUsername(username);
        if (userJson != null) {
            userdataDBClient.delete(userJson);
            log.info("DELETED USER");
        }
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
        UserdataUserJson udUser = userdataDBClient.findByUsername(username);
        Assertions.assertNull(udUser, "Юзер %s был создан".formatted(udUser));
        log.info("EXPECT NOT CREATED AUTH_USER");
        AuthUserEntity authUserEntity = authUserDBClient.findUserByUsername(username);
        Assertions.assertNull(authUserEntity, "Юзер авторизации %s был создан".formatted(authUserEntity));
    }
}

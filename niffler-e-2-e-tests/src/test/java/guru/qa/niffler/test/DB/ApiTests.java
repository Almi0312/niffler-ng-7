package guru.qa.niffler.test.DB;

import guru.qa.niffler.api.SpendApi;
import guru.qa.niffler.data.entity.userdata.FriendshipStatus;
import guru.qa.niffler.data.entity.userdata.UserdataUserEntity;
import guru.qa.niffler.data.repository.auth.impl.AuthUserSpringRepositoryJdbc;
import guru.qa.niffler.data.repository.userdata.UserdataRepository;
import guru.qa.niffler.data.repository.userdata.impl.UserdataHibernateRepository;
import guru.qa.niffler.jupiter.annotation.Category;
import guru.qa.niffler.jupiter.annotation.Spending;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.model.SpendJson;
import guru.qa.niffler.model.UserdataUserJson;
import guru.qa.niffler.service.SpendsClient;
import guru.qa.niffler.service.UsersClient;
import guru.qa.niffler.service.spend.SpendApiClient;
import guru.qa.niffler.service.spend.SpendDBRepositoryClient;
import guru.qa.niffler.service.spend.SpendDBSpringRepositoryClient;
import guru.qa.niffler.service.userdata.UserdataApiClient;
import guru.qa.niffler.service.userdata.UserdataDBSpringRepositoryClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.opentest4j.AssertionFailedError;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static guru.qa.niffler.config.Constants.DEFAULT_PASSWORD;
import static guru.qa.niffler.config.Constants.MAIN_USERNAME;
import static guru.qa.niffler.model.CurrencyValues.RUB;
import static guru.qa.niffler.model.FriendState.FRIEND;
import static guru.qa.niffler.util.RandomDataUtils.randomUsername;

@Slf4j
class ApiTests {

    static UsersClient userDBRepo = new UserdataDBSpringRepositoryClient();
    static UsersClient userApi = new UserdataApiClient();
    static SpendsClient spendDBRepo = new SpendDBSpringRepositoryClient();
    static SpendsClient spendApi = new SpendApiClient();

    @User(
            categories = @Category(
                    name = "api top cource",
                    archived = false),
            spendings = @Spending(
                    category = "api top cource",
                    description = "api cool cource",
                    amount = 100000))
    @Test
    void daoCRUDSpendTest(UserdataUserJson userJson) {
        String categoryName = userJson.testData().categories().getFirst().name();
        String spendDescription = userJson.testData().spendings().getFirst().description();

        SpendJson resultSpend = spendApi.findByUsernameAndDescription(userJson.username(), spendDescription).orElse(null);
        Assertions.assertNotNull(resultSpend);
        Assertions.assertNotNull(resultSpend.category());
        Assertions.assertEquals(spendDescription, resultSpend.description());
        Assertions.assertEquals(categoryName, resultSpend.category().name());
        spendApi.remove(resultSpend);
        spendDBRepo.removeCategory(resultSpend.category());
        Assertions.assertNull(spendDBRepo.findById(userJson.testData().spendings().getFirst().id())
                .orElse(null));
        userDBRepo.remove(userJson);
    }

    @CsvSource(value =
            "api twix, 12345")
    @ParameterizedTest
    void daoCreateAndCheckUserTest(String username, String password) {
        log.info("CREATE USER");
        UserdataUserJson createUserJson = userApi
                .create(username, RUB, password);
        log.info("CHECK USER");
        UserdataUserJson findedUserJson = userApi.findByUsername(createUserJson.username())
                .orElse(null);
        Assertions.assertNotNull(findedUserJson);
        Assertions.assertEquals(createUserJson.id(), findedUserJson.id());
        log.info("DELETE USER");
        userDBRepo.remove(createUserJson);
        Assertions.assertNull(userDBRepo.findByUsername(createUserJson.username()).orElse(null));
    }
}

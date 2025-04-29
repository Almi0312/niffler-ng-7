package guru.qa.niffler.test.web;

import com.codeborne.selenide.Selenide;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.jupiter.annotation.ScreenShotTest;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.jupiter.annotation.meta.WebTest;
import guru.qa.niffler.jupiter.annotation.Spending;
import guru.qa.niffler.model.rest.UserdataUserJson;
import guru.qa.niffler.page.LoginPage;
import guru.qa.niffler.page.MainPage;
import guru.qa.niffler.util.ScreenDiffResult;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import ru.yandex.qatools.ashot.comparison.ImageDiff;
import ru.yandex.qatools.ashot.comparison.ImageDiffer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.codeborne.selenide.Selenide.$;
import static guru.qa.niffler.config.Constants.MAIN_USERNAME;
import static guru.qa.niffler.util.RandomDataUtils.randomCategoryName;
import static guru.qa.niffler.util.RandomDataUtils.randomSpendName;
import static org.junit.jupiter.api.Assertions.assertFalse;

@WebTest
public class SpendingWebTest {

    private static final Config CFG = Config.getInstance();

    @User(spendings = @Spending(
            category = "Sugar",
            description = "daaeaweqew",
            amount = 59877)
    )
    @Test
    void categoryDescriptionShouldBeChangedFromTable(UserdataUserJson userJson) {
        final String newDescription = "Обучение Niffler Next Generation";
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(userJson.username(), userJson.testData().password())
                .getTableSpendings()
                .editSpending(userJson.testData().spendings().getFirst().description())
                .setNewSpendingDescription(newDescription)
                .save();
        new MainPage()
                .checkAlertMessage("Spending is edited successfully")
                .getTableSpendings()
                .checkTableContains(newDescription);
    }

    @User(spendings = @Spending(
            category = "Sugar",
            description = "daaeaweqew",
            amount = 59877)
    )
    @Test
    void SpendShouldBeDeleteFromTableAndCheck(UserdataUserJson userJson) {
        final String newDescription = "Обучение Niffler Next Generation";
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(userJson.username(), userJson.testData().password())
                .getTableSpendings()
                .deleteSpending(userJson.testData().spendings().getFirst().description());
        new MainPage()
                .checkAlertMessage("Spendings succesfully deleted")
                .getTableSpendings()
                .checkTableNoContains(newDescription);
    }

    @Test
    @User()
    void addNewSpending(UserdataUserJson userJson) {
        String spendName = randomSpendName();
        String categoryName = randomCategoryName();
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(userJson.username(), userJson.testData().password())
                .getHeader()
                .addSpendingPage()
                .setNewAmount("100000")
                .setNewCategoryDescription(categoryName)
                .setNewSpendingDescription(spendName)
                .save();
        new MainPage()
                .checkAlertMessage("New spending is successfully created")
                .getTableSpendings()
                .checkTableContains(spendName);
    }

    @Test
    @User(spendings = @Spending(category = "Sugar",
            description = "spend api",
            amount = 10000
    ))
    void addNewSpendingWithApi(UserdataUserJson userJson) {
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(userJson.username(), userJson.testData().password())
                .getTableSpendings()
                .checkTableContains(userJson.testData().spendings().getFirst().description());
    }

    @User(username = MAIN_USERNAME)
    @ScreenShotTest(pathToExpFile = "img/expected-stat.png")
    void checkStatComponentTest(UserdataUserJson userJson, BufferedImage expected) throws IOException {
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(userJson.username(), userJson.testData().password());
        BufferedImage actual = ImageIO.read($("canvas[role='img']").screenshot());
        assertFalse(new ScreenDiffResult(expected, actual));
    }
}

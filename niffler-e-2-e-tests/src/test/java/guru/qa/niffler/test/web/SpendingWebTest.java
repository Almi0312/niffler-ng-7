package guru.qa.niffler.test.web;

import com.codeborne.selenide.Selenide;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.config.Constants;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.jupiter.annotation.meta.WebTest;
import guru.qa.niffler.jupiter.annotation.Spending;
import guru.qa.niffler.model.UserdataUserJson;
import guru.qa.niffler.page.LoginPage;
import guru.qa.niffler.page.MainPage;
import guru.qa.niffler.util.RandomDataUtils;
import org.junit.jupiter.api.Test;

import static guru.qa.niffler.config.Constants.MAIN_PASSWORD;
import static guru.qa.niffler.config.Constants.MAIN_USERNAME;
import static guru.qa.niffler.util.RandomDataUtils.randomCategoryName;
import static guru.qa.niffler.util.RandomDataUtils.randomSpendName;

@WebTest
public class SpendingWebTest {

    private static final Config CFG = Config.getInstance();

    @User(
            username = MAIN_USERNAME,
            spendings = @Spending(
                    category = "Sugar",
                    description = "daaeaweqew",
                    amount = 59877)
    )
    @Test
    void categoryDescriptionShouldBeChangedFromTable(UserdataUserJson userJson) {
        final String newDescription = "Обучение Niffler Next Generation";

        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(MAIN_USERNAME, MAIN_PASSWORD)
                .editSpending(userJson.testData().spendings().getFirst().description())
                .setNewSpendingDescription(newDescription)
                .save();
        new MainPage().checkThatTableContainsSpending(newDescription);
    }

    @Test
    void addNewSpending() {
        String spendName = randomSpendName();
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(MAIN_USERNAME, MAIN_PASSWORD)
                .goOnAddSpendingPage()
                .setNewAmount("100000")
                .setNewCategoryDescription()
                .setNewSpendingDescription(spendName)
                .save();
        new MainPage().checkThatTableContainsSpending(spendName);
    }
}

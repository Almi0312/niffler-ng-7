package guru.qa.niffler.test.web;

import com.codeborne.selenide.Selenide;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.jupiter.annotation.Category;
import guru.qa.niffler.jupiter.annotation.ScreenShotTest;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.jupiter.annotation.meta.WebTest;
import guru.qa.niffler.jupiter.annotation.Spending;
import guru.qa.niffler.model.rest.UserdataUserJson;
import guru.qa.niffler.page.LoginPage;
import guru.qa.niffler.page.MainPage;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;

import static guru.qa.niffler.util.RandomDataUtils.randomCategoryName;
import static guru.qa.niffler.util.RandomDataUtils.randomSpendName;
import static java.lang.String.format;

@WebTest
public class SpendingWebTest {

    private static final Config CFG = Config.getInstance();
    private static final Logger log = LoggerFactory.getLogger(SpendingWebTest.class);

    @Test
    @User(spendings = @Spending(
            category = "Java",
            description = "spend api",
            amount = 10000
    ))
    void addNewSpendingWithApi(UserdataUserJson userJson) {
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(userJson.username(), userJson.testData().password())
                .getTableSpendings()
                .checkTableContains(userJson.testData().spendings().getFirst().description());
    }

    @User(spendings = @Spending(
            category = "Java Advanced",
            description = "advanced java study",
            amount = 90000)
    )
    @ScreenShotTest(pathToExpFile = "img/spends/edit-spend-expected-stat.png")
    void spendDescriptionShouldBeChangedFromTable(UserdataUserJson userJson, BufferedImage expected) {
        final String newDescription = "Обучение Niffler Next Generation";
        String amount = "80000";
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(userJson.username(), userJson.testData().password())
                .getStatComponent()
                .checkDiagramNoCorrespondsScreenshot(expected);
        new MainPage()
                .getTableSpendings()
                .editSpending(userJson.testData().spendings().getFirst().description())
                .setNewSpendingDescription(newDescription)
                .setNewAmount(amount)
                .save();
        new MainPage()
                .checkAlertMessage("Spending is edited successfully")
                .getTableSpendings()
                .checkTableContains(newDescription);
        new MainPage()
                .getStatComponent()
                .checkTextInBubbles(format("%s %s ₽",
                        userJson.testData().spendings().getFirst().category().name(),
                        amount))
                .checkDiagramCorrespondsScreenshot(expected);
    }

    @User(spendings = {
            @Spending(category = "SQL beginner",
                    description = "Begin oracle study",
                    amount = 98989),
            @Spending(category = "SQL beginner",
                    description = "Begin oracle1 study",
                    amount = 89989)
    })
    @ScreenShotTest(pathToExpFile = "img/spends/remove-spend-expected-stat.png")
    void SpendShouldBeDeleteFromTableAndCheck(UserdataUserJson userJson, BufferedImage expected) {
        String lastSpend = userJson.testData().spendings().getLast().description();
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(userJson.username(), userJson.testData().password())
                .getStatComponent()
                .checkDiagramNoCorrespondsScreenshot(expected);
        new MainPage()
                .getTableSpendings()
                .deleteSpending(lastSpend);
        new MainPage()
                .checkAlertMessage("Spendings succesfully deleted")
                .getTableSpendings()
                .checkTableNoContains(lastSpend);
        new MainPage()
                .getStatComponent()
                .checkTextInBubbles(format("%s %s ₽",
                        userJson.testData().spendings().getFirst().category().name(), userJson.testData().spendings().getFirst().amount()))
                .checkDiagramCorrespondsScreenshot(expected);
    }

    @User(spendings = @Spending(
            category = "Java Begin",
            description = "Java for u beginner",
            amount = 43555))
    @ScreenShotTest(pathToExpFile = "img/spends/add-spend-expected-stat.png")
    void addNewSpending(UserdataUserJson userJson, BufferedImage expected) {
        String spendName = randomSpendName();
        String categoryName = randomCategoryName();
        String amount = "100000";
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(userJson.username(), userJson.testData().password())
                .getStatComponent()
                .checkDiagramNoCorrespondsScreenshot(expected);
        new MainPage()
                .getHeader()
                .addSpendingPage()
                .setNewAmount(amount)
                .setNewCategoryDescription(categoryName)
                .setNewSpendingDescription(spendName)
                .save();
        new MainPage()
                .checkAlertMessage("New spending is successfully created")
                .getTableSpendings()
                .checkTableContains(spendName);
        new MainPage()
                .getStatComponent()
                .checkTextInBubbles(format("%s %s ₽",
                                userJson.testData().spendings().getFirst().category().name(), userJson.testData().spendings().getFirst().amount()),
                        format("%s %s ₽",
                                categoryName, amount))
                .checkDiagramCorrespondsScreenshot(expected);
    }

    @User(categories = @Category(
            name = "Kafka Advanced",
            archived = true),
            spendings = {
                    @Spending(
                            category = "Kafka Advanced",
                            description = "Story about Kafka",
                            amount = 100),
                    @Spending(
                            category = "Kafka Beginner",
                            description = "Story about Kafka",
                            amount = 100)}
    )
    @ScreenShotTest(pathToExpFile = "img/spends/default-expected-stat.png")
    void checkStatComponentTest(UserdataUserJson userJson, BufferedImage expected) {
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(userJson.username(), userJson.testData().password());
        new MainPage()
                .getTableSpendings()
                .checkTableContains(userJson.testData().spendings().getFirst().description());
        new MainPage()
                .getStatComponent()
                .checkDiagramCorrespondsScreenshot(expected);
    }
}

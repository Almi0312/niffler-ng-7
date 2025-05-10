package guru.qa.niffler.test.web;

import com.codeborne.selenide.Selenide;
import guru.qa.niffler.condition.Color;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.jupiter.annotation.*;
import guru.qa.niffler.jupiter.annotation.meta.WebTest;
import guru.qa.niffler.model.rest.UserdataUserJson;
import guru.qa.niffler.page.LoginPage;
import guru.qa.niffler.page.MainPage;
import guru.qa.niffler.page.component.StatComponent;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static guru.qa.niffler.util.RandomDataUtils.randomCategoryName;
import static guru.qa.niffler.util.RandomDataUtils.randomSpendName;
import static java.lang.String.format;

@WebTest
public class SpendingWebTest {

    private static final Config CFG = Config.getInstance();

    @Test
    @ApiLogin
    @User(spendings = @Spending(
            category = "Java",
            description = "spend api",
            amount = 10000
    ))
    void addNewSpendingWithApi(UserdataUserJson userJson) {
        new MainPage()
                .getTableSpendings()
                .checkTableContains(userJson.testData().spendings().getFirst().description());
    }

    @ScreenShotTest(pathToExpFile = "img/spends/edit-spend-expected-stat.png")
    @ApiLogin
    @User(spendings = @Spending(
            category = "Java Advanced",
            description = "advanced java study",
            amount = 90000)
    )
    void spendDescriptionShouldBeChangedFromTable(UserdataUserJson userJson, BufferedImage expected) {
        final String newDescription = "Обучение Niffler Next Generation";
        String amount = "80000";
        MainPage mainPage = new MainPage();
        mainPage
                .getStatComponent()
                .checkDiagramNoCorrespondsScreenshot(expected);
        mainPage
                .getTableSpendings()
                .editSpending(userJson.testData().spendings().getFirst().description())
                .setNewSpendingDescription(newDescription)
                .setNewAmount(amount)
                .save();
        mainPage
                .checkAlertMessage("Spending is edited successfully")
                .getTableSpendings()
                .checkTableContains(newDescription);
        mainPage
                .getStatComponent()
                .checkTextInBubbles(format("%s %s ₽",
                        userJson.testData().spendings().getFirst().category().name(),
                        amount))
                .checkDiagramCorrespondsScreenshot(expected);
    }

    @ScreenShotTest(pathToExpFile = "img/spends/remove-spend-expected-stat.png")
    @ApiLogin
    @User(spendings = {
            @Spending(category = "SQL beginner",
                    description = "Begin oracle study",
                    amount = 98989),
            @Spending(category = "SQL beginner",
                    description = "Begin oracle1 study",
                    amount = 89989)
    })
    void SpendShouldBeDeleteFromTableAndCheck(UserdataUserJson userJson, BufferedImage expected) {
        String lastSpend = userJson.testData().spendings().getLast().description();
        MainPage mainPage = new MainPage();
        mainPage
                .getStatComponent()
                .checkDiagramNoCorrespondsScreenshot(expected);
        mainPage
                .getTableSpendings()
                .deleteSpending(lastSpend);
        mainPage
                .checkAlertMessage("Spendings succesfully deleted")
                .getTableSpendings()
                .checkTableNoContains(lastSpend);
        mainPage
                .getStatComponent()
                .checkTextInBubbles(format("%s %s ₽",
                        userJson.testData().spendings().getFirst().category().name(),
                        userJson.testData().spendings().getFirst().amount())
                )
                .checkDiagramCorrespondsScreenshot(expected);
    }

    @ScreenShotTest(pathToExpFile = "img/spends/add-spend-expected-stat.png")
    @ApiLogin
    @User(spendings = @Spending(
            category = "Java Begin",
            description = "Java for u beginner",
            amount = 43555))
    void addNewSpending(UserdataUserJson userJson, BufferedImage expected) {
        String spendName = randomSpendName();
        String categoryName = randomCategoryName();
        String amount = "100000";
        MainPage mainPage = new MainPage();
        mainPage
                .getStatComponent()
                .checkDiagramNoCorrespondsScreenshot(expected);
        mainPage
                .getHeader()
                .addSpendingPage()
                .setNewAmount(amount)
                .setNewCategoryDescription(categoryName)
                .setNewSpendingDescription(spendName)
                .save();
        mainPage
                .checkAlertMessage("New spending is successfully created")
                .getTableSpendings()
                .checkTableContains(spendName);
        mainPage
                .getStatComponent()
                .checkBubbles(
                        new StatComponent.Bubble(Color.green, format("%s %s ₽",
                                userJson.testData().spendings().getFirst().category().name(),
                                userJson.testData().spendings().getFirst().amount().intValue())),
                        new StatComponent.Bubble(Color.yellow, format("%s %s ₽",
                                categoryName, amount))
                )
                .checkDiagramCorrespondsScreenshot(expected);
    }

    @ScreenShotTest(pathToExpFile = "img/spends/default-expected-stat.png")
    @ApiLogin
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
    void checkStatComponentTest(UserdataUserJson userJson, BufferedImage expected) {
        MainPage mainPage = new MainPage();
        mainPage
                .getTableSpendings()
                .checkTableContains(userJson.testData().spendings().getFirst().description());
        mainPage
                .getStatComponent()
                .checkDiagramCorrespondsScreenshot(expected);
    }

    @Test
    @ApiLogin
    @User(categories = @Category(
            name = "Электроника",
            archived = true),
            spendings = {
                    @Spending(
                            category = "Электроника",
                            description = "Розетка",
                            amount = 200),
                    @Spending(
                            category = "Электроника",
                            description = "Удлинитель",
                            amount = 555),
                    @Spending(
                            category = "Электроника",
                            description = "Батарейка",
                            amount = 300)}
    )
    void checkAllSpendByCategoryTest(UserdataUserJson userJson) {
        new MainPage()
                .getTableSpendings()
                .searchSpendingByDescription("Электроника")
                .checkAllSpends(userJson.testData().spendings());
    }
}

package guru.qa.niffler.page;

import com.codeborne.selenide.SelenideElement;
import guru.qa.niffler.page.component.Header;
import guru.qa.niffler.page.component.SpendingTable;
import guru.qa.niffler.page.profileInfo.FriendsPage;
import guru.qa.niffler.page.profileInfo.ProfilePage;
import guru.qa.niffler.page.profileInfo.AllPeoplePage;
import io.qameta.allure.Step;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

@ParametersAreNonnullByDefault
public class MainPage extends BasePage<MainPage> {
    private final SelenideElement headerForSpending = $x(".//div[@id='spendings']/h2");
    private final SpendingTable tableSpendings = new SpendingTable($("#spendings"));
    private final SelenideElement headerForDiagram = $x(".//div[@id='stat']/h2");
    private final SelenideElement diagram = $x(".//div[@id='stat']//canvas[@role='img']");
    private final SelenideElement searchInput = $x(".//input[@aria-label='search']");

    private final SelenideElement userAvatar = $x(".//button[@aria-label='Menu']");
    private final Header header = new Header();

    public @Nonnull EditSpendingPage editSpending(String spendingDescription) {
        return tableSpendings.editSpending(spendingDescription);
    }

    public @Nonnull MainPage deleteSpending(String spendingDescription) {
        tableSpendings.deleteSpending(spendingDescription);
        return this;
    }

    public @Nonnull MainPage checkThatTableContainsSpending(String spendingDescription) {
        tableSpendings.checkTableContains(spendingDescription);
        return this;
    }

    public @Nonnull MainPage checkThatTableNoContainsSpending(String spendingDescription) {
        tableSpendings.checkTableContains(spendingDescription);
        return this;
    }

    @Step("Проверить что диаграмма присутствует")
    public @Nonnull MainPage checkDiagramStatistics() {
        diagram.shouldBe(visible);
        return this;
    }

    @Step("Проверить что таблица присутствует")
    public @Nonnull MainPage checkTableSpending() {
        tableSpendings.getElement().shouldBe(visible);
        return this;
    }

    public @Nonnull ProfilePage goOnProfilePage() {
        return header.toProfilePage();
    }

    public @Nonnull FriendsPage goOnFriendsPage() {
        return header.toFriendsPage();
    }

    public @Nonnull AllPeoplePage goOnAllPeoplePage() {
        return header.toAllPeoplePage();
    }

    public @Nonnull EditSpendingPage goOnAddSpendingPage() {
        return header.addSpendingPage();
    }
}

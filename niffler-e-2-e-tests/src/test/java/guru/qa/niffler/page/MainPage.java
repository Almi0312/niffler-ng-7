package guru.qa.niffler.page;

import com.codeborne.selenide.SelenideElement;
import guru.qa.niffler.page.component.Header;
import guru.qa.niffler.page.component.SpendingTable;
import guru.qa.niffler.page.component.StatComponent;
import guru.qa.niffler.page.profileInfo.FriendsPage;
import guru.qa.niffler.page.profileInfo.ProfilePage;
import guru.qa.niffler.page.profileInfo.AllPeoplePage;
import io.qameta.allure.Step;
import lombok.Getter;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

@ParametersAreNonnullByDefault
public class MainPage extends BasePage<MainPage> {

    public static final String URL = CFG.frontUrl() + "main";

    private final SelenideElement headerForSpending = $x(".//div[@id='spendings']/h2");
    @Getter
    private final SpendingTable tableSpendings = new SpendingTable($("#spendings"));
    private final SelenideElement headerForDiagram = $x(".//div[@id='stat']/h2");
    private final SelenideElement searchInput = $x(".//input[@aria-label='search']");

    private final SelenideElement userAvatar = $x(".//button[@aria-label='Menu']");
    @Getter
    private final Header header = new Header();
    @Getter
    private final StatComponent statComponent = new StatComponent();

    @Step("Проверить что диаграмма присутствует")
    public @Nonnull MainPage checkDiagramStatistics() {
        statComponent.getElement().shouldBe(visible);
        return this;
    }

    @Step("Проверить что таблица присутствует")
    public @Nonnull MainPage checkTableSpending() {
        tableSpendings.getElement().shouldBe(visible);
        return this;
    }

    @Override
    @Step("Check that page is loaded")
    @Nonnull
    public MainPage checkThatPageLoaded() {
        header.getElement().should(visible).shouldHave(text("Niffler"));
        statComponent.getElement().should(visible).shouldHave(text("Statistics"));
        tableSpendings.getElement().should(visible).shouldHave(text("History of Spendings"));
        return this;
    }

}

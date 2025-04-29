package guru.qa.niffler.page.profileInfo;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import guru.qa.niffler.page.BasePage;
import guru.qa.niffler.page.component.SearchField;
import io.qameta.allure.Step;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.*;
import static java.lang.String.format;

@ParametersAreNonnullByDefault
public class FriendsPage extends BasePage<FriendsPage> {
    private final ElementsCollection tabs = $$x(".//div[@role='tablist']/*");
    private final SelenideElement selectedTab = tabs.find(attributeMatching(
            "class", ".*Mui-selected.*"));
    private final SelenideElement friendsTable = $x(".//table[./tbody[@id='friends']]");
    private final SelenideElement requestTable = $x(".//table[./tbody[@id='requests']]");
    private final SelenideElement emptyMessageInsteadTable = $x(".//p[contains(@class, 'MuiTypography-root')]");
    private final SearchField searchInput = new SearchField($x(".//input[@aria-label='search']"));
    private final SelenideElement declineButton = $x(".//button[normalize-space(text()) = 'Decline']");

    @Step("Проверить что вкладка {0} присутствует")
    public @Nonnull FriendsPage checkTabsByName(String tabName) {
        tabs.find(text(tabName)).shouldBe(visible);
        return this;
    }

    @Step("Проверить что вкладка {0} выбрана")
    public @Nonnull FriendsPage checkSelectedTabByName(String tabName) {
        selectedTab.shouldBe(text(tabName).because(format(
                "Название выбранной вкладки %s не совпадает с актуальной - %s", selectedTab.text(), tabName)));
        return this;
    }

    @Step("Нажать на вкладку {0}")
    public @Nonnull FriendsPage clickTabsByName(String tabName) {
        tabs.find(text(tabName)).shouldBe(visible).click();
        return this;
    }

    @Step("Найти пользователя {0}")
    public void sendUserInfoInSearchLine(String spendingDescription) {
        searchInput.search(spendingDescription);
    }

    @Step("Проверить что пользователь-друг {0} присутствует в таблице")
    public @Nonnull FriendsPage checkFriendInTable(String friendName) {
        Selenide.refresh();
        friendsTable.$$x(".//tr").find(text(friendName)).shouldBe(visible);
        return this;
    }

    @Step("Проверить что пользователь-друг {0} отсутствует в таблице")
    public @Nonnull FriendsPage checkNotFriendInTable(String friendName) {
        Selenide.refresh();
        friendsTable.$$x(".//tr").find(text(friendName)).shouldNotBe(visible);
        return this;
    }

    @Step("Проверить что приглашение в друзья присутствует для пользователя {0}")
    public @Nonnull FriendsPage checkIncomeInTable(String incomeName) {
        sendUserInfoInSearchLine(incomeName);
        requestTable.$$x(".//tr").find(text(incomeName)).shouldBe(allOf(visible,
                text("Accept"),
                text("Decline")));
        return this;
    }

    @Step("Нажать кнопку 'Accept' для юзера {0}")
    public @Nonnull FriendsPage acceptIncomeInvication(String incomeName) {
        requestTable.$$x(".//tr").find(text(incomeName)).shouldBe(visible)
                .$(byText("Accept")).shouldBe(visible)
                .click();
        return this;
    }

    @Step("Нажать кнопку 'Decline' для юзера {0}")
    public @Nonnull FriendsPage declineIncomeInvication(String incomeName) {
        requestTable.$$x(".//tr").find(text(incomeName)).shouldBe(visible)
                .$(byText("Decline")).shouldBe(visible)
                .click();
        return this;
    }

    @Step("Проверить что таблица пуста")
    public @Nonnull FriendsPage checkEmptyTable() {
        String emptyMessageStr = "There are no users yet";
        friendsTable.shouldNotBe(exist);
        requestTable.shouldNotBe(exist);
        emptyMessageInsteadTable.shouldBe(allOf(visible,
                text(emptyMessageStr).because(format(
                        "Сообщение с текстом [%s] не отображается. Актуальный - [%s]",
                        emptyMessageStr, emptyMessageInsteadTable.getText()))));
        return this;
    }
}

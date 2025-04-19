package guru.qa.niffler.page.profileInfo;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;
import static java.lang.String.format;

public class FriendsPage {
    private final ElementsCollection tabs = $$x(".//div[@role='tablist']/*");
    private final SelenideElement selectedTab = tabs.find(attributeMatching(
            "class",".*Mui-selected.*"));
    private final SelenideElement friendsTable = $x(".//table[./tbody[@id='friends']]");
    private final SelenideElement requestTable = $x(".//table[./tbody[@id='requests']]");
    private final SelenideElement emptyMessageInsteadTable = $x(".//p[contains(@class, 'MuiTypography-root')]");
    private final SelenideElement searchInput = $x(".//input[@aria-label='search']");

    public FriendsPage checkTabsByName(String tabName) {
        tabs.find(text(tabName)).shouldBe(visible);
        return this;
    }

    public FriendsPage checkSelectedTabByName(String tabName) {
        selectedTab.shouldBe(text(tabName).because(format(
                "Название выбранной вкладки %s не совпадает с актуальной - %s", selectedTab.text(), tabName)));
        return this;
    }

    public FriendsPage clickTabsByName(String tabName) {
        tabs.find(text(tabName)).shouldBe(visible).click();
        return this;
    }

    public void sendUserInfoInSearchLine(String spendingDescription) {
        searchInput.shouldBe(exist).setValue(spendingDescription).pressEnter();
    }

    public FriendsPage checkFriendInTable(String friendName) {
        sendUserInfoInSearchLine(friendName);
        friendsTable.$$x(".//tr").find(text(friendName)).shouldBe(visible);
        return this;
    }

    public FriendsPage checkIncomeInTable(String incomeName) {
        sendUserInfoInSearchLine(incomeName);
        requestTable.$$x(".//tr").find(text(incomeName)).shouldBe(allOf(visible,
                text("Accept"),
                text("Decline")));
        return this;
    }

    public FriendsPage checkEmptyTable() {
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

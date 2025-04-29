package guru.qa.niffler.page.profileInfo;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import guru.qa.niffler.page.BasePage;
import io.qameta.allure.Step;

import javax.annotation.Nonnull;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Selenide.$;

public class AllPeoplePage extends BasePage<AllPeoplePage> {

    public static final String URL = CFG.frontUrl() + "people/all";

    private final ElementsCollection tabs = $$x(".//div[@role='tablist']/*");
    private final SelenideElement selectedTab = tabs.find(attributeMatching(
            "class", ".*Mui-selected.*"));
    private final SelenideElement allPeopleTable = $x(".//table[./tbody[@id='all']]");

    private final SelenideElement allTab = $("a[href='/people/all']");
    private final SelenideElement peopleTable = $("#all");
    private final SelenideElement pagePrevBtn = $("#page-prev");
    private final SelenideElement pageNextBtn = $("#page-next");

    @Step("Проверить что заявка в друзья отправлена пользователю {0}")
    public AllPeoplePage checkOutcomeInTable(String outcomeName) {
        String textInsteadButton = "Waiting...";
        allPeopleTable.$$x(".//tr").find(text(outcomeName)).shouldBe(allOf(visible,
                text(textInsteadButton)));
        return this;
    }

    @Override
    @Step("Check that the page is loaded")
    @Nonnull
    public AllPeoplePage checkThatPageLoaded() {
        selectedTab.shouldBe(Condition.visible);
        allTab.shouldBe(Condition.visible);
        return this;
    }
}

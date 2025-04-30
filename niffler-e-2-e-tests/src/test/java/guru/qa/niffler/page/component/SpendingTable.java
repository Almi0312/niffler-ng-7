package guru.qa.niffler.page.component;

import com.codeborne.selenide.SelenideElement;
import guru.qa.niffler.model.DataFilter;
import guru.qa.niffler.page.EditSpendingPage;
import io.qameta.allure.Step;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$$x;
import static com.codeborne.selenide.Selenide.$x;

public class SpendingTable extends BaseComponent<SpendingTable> {

    private final SearchField searchField = new SearchField($x(
            ".//input[@aria-label='search']"));
    private final String rows = ".//tbody//tr";

    public SpendingTable(SelenideElement self) {
        super(self);
    }

    @Step("Выбрать период {0}")
    public SpendingTable selectPeriod(DataFilter period) {
        dataFilter().shouldBe(visible).click();
        $$x(".//ul[@role='listbox']/li")
                .find(text(period.dateFilter)).shouldBe(visible).click();
        return this;
    }

    @Step("Перейти на страницу редактирование траты(spend) {0}")
    public EditSpendingPage editSpending(String description) {
        searchSpendingByDescription(description);
        clickByCell(description, 5);
        return new EditSpendingPage();
    }

    @Step("Удалить трату(spend) {0}")
    public SpendingTable deleteSpending(String description) {
        searchSpendingByDescription(description)
                .clickByCell(description, 0)
                .getButtonByText("Delete").click();
        getModalWindow().$x(".//button[normalize-space(text()) = 'Delete']").shouldBe(enabled).click();
        return this;
    }

    @Step("Найти трату(spend) {0}")
    public SpendingTable searchSpendingByDescription(String description) {
        searchField.search(description);
        return this;
    }

    @Step("Проверить что в таблице присутствуют траты {0}")
    public SpendingTable checkTableContains(String... expectedSpends) {
        for (String spend : expectedSpends) {
            searchSpendingByDescription(spend);
            self.$$x(rows).find(text(spend)).shouldBe(visible);
        }
        return this;
    }

    @Step("Проверить что в таблице отсутствуют траты {0}")
    public SpendingTable checkTableNoContains(String... expectedSpends) {
        for (String spend : expectedSpends) {
            searchSpendingByDescription(spend);
            self.$$x(rows).find(text(spend)).shouldNotBe(visible);
        }
        return this;
    }

    @Step("Проверить что в таблице кол-во строк равно {0}")
    public SpendingTable checkTableSize(int expectedSize) {
        self.$$x(rows).shouldBe(size(expectedSize));
        return this;
    }

    @Step("Нажать на {1} ячейку в строке с тратой(spend) {0}")
    public SpendingTable clickByCell(String description, int index) {
        self.$$x(rows)
                .find(text(description)).$$("td").get(index)
                .shouldBe(visible)
                .click();
        return this;
    }

    protected SelenideElement dataFilter() {
        return $x(".//div[@id='period']");
    }

    protected SelenideElement getModalWindow() {
        return $x(".//*[@role='dialog']");
    }

}

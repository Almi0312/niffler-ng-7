package guru.qa.niffler.page;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import guru.qa.niffler.page.component.Calendar;
import io.qameta.allure.Step;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import java.util.Objects;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;
import static guru.qa.niffler.util.RandomDataUtils.getRandomNumber;

@ParametersAreNonnullByDefault
public class EditSpendingPage {
    private final SelenideElement descriptionInput = $("#description");
    private final SelenideElement categoryInput = $("#category");
    private final ElementsCollection categories = $$x(".//ul//li[@role='menuitem']");
    private final SelenideElement amountInput = $("#amount");
    private final SelenideElement saveBtn = $("#save");

    private final Calendar calendar = new Calendar($(".SpendingCalendar"));

    @Step("Ввести в поле 'Description' {0}")
    public @Nonnull EditSpendingPage setNewSpendingDescription(String description) {
        descriptionInput.clear();
        descriptionInput.setValue(description);
        return this;
    }

    @Step("Ввести в поле 'Category' {0}")
    public @Nonnull EditSpendingPage setNewCategoryDescription(String description) {
        categoryInput.clear();
        categoryInput.shouldBe(value(""));
        categoryInput.setValue(description);
        categoryInput.shouldBe(value(description));
        return this;
    }

    @Step("Ввести в поле 'Amount' {0}")
    public @Nonnull EditSpendingPage setNewAmount(String description) {
        amountInput.clear();
        amountInput.setValue(description);
        return this;
    }

    @Step("Нажать кнопку 'Add'")
    public void save() {
        saveBtn.click();
    }

}

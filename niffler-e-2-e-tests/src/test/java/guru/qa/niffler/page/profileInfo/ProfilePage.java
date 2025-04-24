package guru.qa.niffler.page.profileInfo;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import guru.qa.niffler.config.Config;
import io.qameta.allure.Step;
import guru.qa.niffler.page.BasePage;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$$x;
import static com.codeborne.selenide.Selenide.$x;

public class ProfilePage extends BasePage<ProfilePage> {

    public static String url = Config.getInstance().frontUrl() + "profile";
    private final SelenideElement archiveButton = $x(".//button[@aria-label = 'Archive category']");
    private final SelenideElement showArchiveCategories = $x(
            ".//*[./*[normalize-space(text()) = 'Show archived']]");
    private final ElementsCollection archivedCategoriesName = $$x(
            ".//*[./button[contains(@aria-label, 'category')]]/preceding-sibling::*");
    private final SelenideElement nameInput = $x(".//*[@id='name']");
    private final SelenideElement saveButton = $x(
            ".//*[normalize-space(text()) = 'Save changes']/ancestor-or-self::button");


    @Step("Нажать на переключатель 'Show archived'")
    public ProfilePage activatedShowArchiveCategory() {
        showArchiveCategories.shouldBe(visible).click();
        return this;
    }

    @Step("Проверить что в списке категорий присутвует {0}")
    public ProfilePage checkNameSpendInCategoryList(String name) {
        archivedCategoriesName.find(text(name)).shouldBe(visible);
        return this;
    }

    @Step("Сохранить изменения")
    public ProfilePage saveChanges() {
        saveButton.shouldBe(visible).click();
        return this;
    }

    @Step("Ввести в поле 'Name' {0}")
    public ProfilePage setValueInFieldName(String name) {
        nameInput.clear();
        nameInput.setValue(name);
        return this;
    }

    @Step("Проверить что в поле 'Name' введено {0}")
    public ProfilePage checkValueInFieldName(String name) {
        nameInput.shouldBe(value(name));
        return this;
    }

    public ProfilePage() {
    }
}

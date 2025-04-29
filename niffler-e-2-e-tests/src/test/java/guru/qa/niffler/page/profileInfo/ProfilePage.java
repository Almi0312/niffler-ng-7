package guru.qa.niffler.page.profileInfo;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import guru.qa.niffler.config.Config;
import io.qameta.allure.Step;
import guru.qa.niffler.page.BasePage;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

public class ProfilePage extends BasePage<ProfilePage> {

    public static final String URL = CFG.frontUrl() + "profile";

    private final SelenideElement avatar = $("#image__input").parent().$("img");
    private final SelenideElement archiveButton = $x(".//button[@aria-label = 'Archive category']");
    private final SelenideElement showArchiveCategories = $x(
            ".//*[./*[normalize-space(text()) = 'Show archived']]");
    private final ElementsCollection archivedCategoriesName = $$x(
            ".//*[./button[contains(@aria-label, 'category')]]/preceding-sibling::*");
    private final SelenideElement nameInput = $x(".//*[@id='name']");
    private final SelenideElement saveButton = $x(
            ".//*[normalize-space(text()) = 'Save changes']/ancestor-or-self::button");

    @Step("Check photo")
    @Nonnull
    public ProfilePage checkPhoto(String path) throws IOException {
        final byte[] photoContent;
        try (InputStream is = new ClassPathResource(path).getInputStream()) {
            photoContent = Base64.getEncoder().encode(is.readAllBytes());
        }
        avatar.should(attribute("src", new String(photoContent, StandardCharsets.UTF_8)));
        return this;
    }

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

    @Override
    @Step("Check that page is loaded")
    @Nonnull
    public ProfilePage checkThatPageLoaded() {
        saveButton.should(visible);
        return this;
    }

    public ProfilePage() {
    }
}

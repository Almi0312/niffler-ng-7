package guru.qa.niffler.page.component;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import guru.qa.niffler.page.EditSpendingPage;
import guru.qa.niffler.page.LoginPage;
import guru.qa.niffler.page.MainPage;
import guru.qa.niffler.page.profileInfo.AllPeoplePage;
import guru.qa.niffler.page.profileInfo.FriendsPage;
import guru.qa.niffler.page.profileInfo.ProfilePage;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;

public class Header extends BaseComponent<Header> {

    public Header() {
        super($("#root header"));
    }

    @Step("Перейти на страницу друзей пользователя")
    public FriendsPage toFriendsPage() {
        String namePage = "Friends";
        getBtnMenu().shouldBe(visible).click();
        getMenuItems().find(text(namePage)).click();
        return new FriendsPage();
    }

    @Step("Перейти на страницу со всеми пользователями")
    public AllPeoplePage toAllPeoplePage() {
        String namePage = "All People";
        getBtnMenu().shouldBe(visible).click();
        getMenuItems().find(text(namePage)).click();
        return new AllPeoplePage();
    }

    @Step("Перейти на страницу профиля пользователя")
    public ProfilePage toProfilePage() {
        String namePage = "Profile";
        getBtnMenu().shouldBe(visible).click();
        getMenuItems().find(text(namePage)).click();
        return new ProfilePage();
    }

    @Step("Выйти из учетной записи")
    public LoginPage signOut() {
        String namePage = "Sign out";
        getBtnMenu().shouldBe(visible).click();
        getMenuItems().find(text(namePage)).click();
        return new LoginPage();
    }

    @Step("Перейти на страницу добавления расхода(spending)")
    public EditSpendingPage addSpendingPage() {
        link("New spending").shouldBe(visible).click();
        return new EditSpendingPage();
    }

    @Step("Перейти на главную страницу")
    public MainPage toMainPage() {
        logo().shouldBe(visible).click();
        return new MainPage();
    }

    protected final SelenideElement getBtnMenu() {
        return self.$x(".//button[@aria-label='Menu']");
    }

    protected final ElementsCollection getMenuItems() {
        return $$x(".//ul[@role='menu']/li//a");
    }

    protected SelenideElement logo() {
        return $x(".//a[./h1[normalize-space(text()) = 'Niffler']]");
    }

}

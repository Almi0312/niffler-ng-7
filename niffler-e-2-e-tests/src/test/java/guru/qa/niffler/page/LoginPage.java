package guru.qa.niffler.page;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;
import static guru.qa.niffler.actions.FieldActions.setFieldAndCheck;
import static guru.qa.niffler.page.RegisterPage.getRegisterPage;
import static java.lang.String.format;

@ParametersAreNonnullByDefault
public class LoginPage extends BasePage<LoginPage> {

    public static final String URL = CFG.authUrl() + "login";

    private final SelenideElement usernameInput = $("input[name='username']");
    private final SelenideElement passwordInput = $("input[name='password']");
    private final SelenideElement submitButton = $("button[type='submit']");
    private final SelenideElement registerButton = $x(".//a[contains(@class, 'form__register')]");
    private final SelenideElement errorMessage = $x(".//*[contains(@class, 'form__error-container')]");


    @Step("Ввести в поле 'username' {0}")
    public @Nonnull LoginPage setUsername(String username) {
        setFieldAndCheck(usernameInput, username);
        return this;
    }

    @Step("Ввести в поле 'password' {0}")
    public @Nonnull LoginPage setPassword(String password) {
        setFieldAndCheck(passwordInput, password);
        return this;
    }

    @Step("Нажать кнопку 'Log in'")
    public @Nonnull RegisterPage clickRegisterButton() {
        registerButton.shouldBe(visible).click();
        return getRegisterPage();
    }

    @Step("Проверить что текст контроля {0} присутствует")
    public @Nonnull LoginPage checkErrorMessage(String message) {
        errorMessage.shouldHave(text(message).because(format(
                "Вместо текста %s содержится текст %s", message, errorMessage.getText())));
        return this;
    }

    @Step("Ввести учетные данные [{0}, {1}] и войти на сайт")
    public @Nonnull MainPage login(String username, String password) {
        setUsername(username)
                .setPassword(password);
        submitButton.shouldBe(visible).click();
        return new MainPage();
    }

    @Override
    @Step("Check that page is loaded")
    @Nonnull
    public LoginPage checkThatPageLoaded() {
        usernameInput.should(visible);
        passwordInput.should(visible);
        return this;
    }

    public LoginPage() {
    }
}
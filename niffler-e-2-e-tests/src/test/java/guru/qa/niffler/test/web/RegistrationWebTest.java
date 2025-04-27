package guru.qa.niffler.test.web;

import com.codeborne.selenide.Selenide;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.config.Constants;
import guru.qa.niffler.jupiter.annotation.Category;
import guru.qa.niffler.jupiter.annotation.Spending;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.jupiter.annotation.meta.WebTest;
import guru.qa.niffler.model.UserdataUserJson;
import guru.qa.niffler.page.LoginPage;
import org.junit.jupiter.api.Test;

import static guru.qa.niffler.util.RandomDataUtils.randomPassword;
import static guru.qa.niffler.util.RandomDataUtils.randomUsername;
import static java.lang.String.format;


@WebTest
public class RegistrationWebTest {

    private static final Config CFG = Config.getInstance();

    @Test
    void shouldRegisterNewUser() {
        String username = randomUsername();
        String password = randomPassword();

        System.out.println("shouldRegisterNewUser[password] - " + password);
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .clickRegisterButton()
                .setUsername(username)
                .setPassword(password)
                .setPasswordSubmit(password)
                .clickSubmitButton()
                .clickSubmitRegistration()
                .login(username, password)
                .checkDiagramStatistics();
    }

    @Test
    void shouldNotRegisterUserWithExistingUsername() {
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .clickRegisterButton()
                .setUsername(Constants.MAIN_USERNAME)
                .setPassword(Constants.MAIN_PASSWORD)
                .setPasswordSubmit(Constants.MAIN_PASSWORD)
                .clickSubmitButton()
                .checkErrorMessageUsername(format("Username `%s` already exists", Constants.MAIN_USERNAME));
    }

    @Test
    void shouldShowErrorIfPasswordAndConfirmPasswordAreNotEqual() {
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .clickRegisterButton()
                .setUsername(randomUsername())
                .setPassword(randomPassword())
                .setPasswordSubmit(randomPassword())
                .clickSubmitButton()
                .checkErrorMessagePassword("Passwords should be equal");
    }

    @User(
            categories = {
                    @Category(name = "Магазины2", archived = false),
                    @Category(name = "Бары2", archived = true)
            },
            spendings = {
                    @Spending(
                            category = "Обучение2",
                            description = "QA.GURU Advanced 7",
                            amount = 80000
                    )
            }
    )
    @Test
    void mai0nPageShouldBeDisplayedAfterSuccessLogin(UserdataUserJson userJson) {
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(userJson.username(), userJson.testData().password())
                .checkDiagramStatistics()
                .checkTableSpending();
    }

    @Test
    void userShouldStayOnLoginPageAfterLoginWithBadCredentials() {
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(randomUsername(), randomPassword());
        new LoginPage().checkErrorMessage("Неверные учетные данные пользователя");
    }
}

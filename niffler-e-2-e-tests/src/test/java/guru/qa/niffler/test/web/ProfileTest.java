package guru.qa.niffler.test.web;

import com.codeborne.selenide.Selenide;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.jupiter.annotation.ScreenShotTest;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.jupiter.annotation.meta.WebTest;
import guru.qa.niffler.jupiter.annotation.Category;
import guru.qa.niffler.model.rest.UserdataUserJson;
import guru.qa.niffler.page.LoginPage;
import guru.qa.niffler.util.RandomDataUtils;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static guru.qa.niffler.config.Constants.MAIN_PASSWORD;
import static guru.qa.niffler.config.Constants.MAIN_USERNAME;

@WebTest
public class ProfileTest {
    private static final Config CFG = Config.getInstance();

    @Test
    @User(categories = @Category(archived = true))
    void archivedCategoryShouldPresentInCategoriesList(UserdataUserJson user) {
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .getHeader()
                .toProfilePage()
                .activatedShowArchiveCategory()
                .checkNameSpendInCategoryList(
                        user.testData().categories().getFirst().name());
    }

    @Test
    @User(categories = @Category(archived = false))
    void activeCategoryShouldPresentInCategoriesList(UserdataUserJson user) {
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .getHeader()
                .toProfilePage()
                .checkNameSpendInCategoryList(
                        user.testData().categories().getFirst().name());
    }

    @Test
    @User()
    void changeFieldName(UserdataUserJson userJson) {
        String name = RandomDataUtils.randomName();
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(userJson.username(), userJson.testData().password())
                .getHeader()
                .toProfilePage()
                .setValueInFieldName(name)
                .saveChanges()
                .checkAlertMessage("Profile successfully updated")
                .checkValueInFieldName(name);
    }

    @User(username = MAIN_USERNAME)
    @ScreenShotTest(pathToExpFile = "img/avatar.png")
    void checkAvatar(UserdataUserJson userJson, BufferedImage expected) {
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(userJson.username(), MAIN_PASSWORD)
                .getHeader()
                .toProfilePage()
                .checkAvatarCorrespondsScreenshot(expected);
    }
}

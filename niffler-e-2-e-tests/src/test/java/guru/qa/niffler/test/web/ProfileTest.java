package guru.qa.niffler.test.web;

import com.codeborne.selenide.Selenide;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.jupiter.annotation.*;
import guru.qa.niffler.jupiter.annotation.meta.WebTest;
import guru.qa.niffler.model.rest.UserdataUserJson;
import guru.qa.niffler.page.LoginPage;
import guru.qa.niffler.page.profileInfo.ProfilePage;
import guru.qa.niffler.util.RandomDataUtils;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static guru.qa.niffler.config.Constants.*;

@WebTest
class ProfileTest {
    private static final Config CFG = Config.getInstance();

    @Test
    @ApiLogin
    @User(categories = @Category(archived = true))
    void archivedCategoryShouldPresentInCategoriesList(UserdataUserJson user) {
        Selenide.open(ProfilePage.URL, ProfilePage.class)
                .activatedShowArchiveCategory()
                .checkNameSpendInCategoryList(
                        user.testData().categories().getFirst().name());
    }

    @Test
    @ApiLogin
    @User(categories = @Category(archived = false))
    void activeCategoryShouldPresentInCategoriesList(UserdataUserJson user) {
        Selenide.open(ProfilePage.URL, ProfilePage.class)
                .checkNameSpendInCategoryList(
                        user.testData().categories().getFirst().name());
    }

    @Test
    @ApiLogin
    @User
    void changeFieldName(UserdataUserJson userJson) {
        String name = RandomDataUtils.randomName();
        Selenide.open(ProfilePage.URL, ProfilePage.class)
                .setValueInFieldName(name)
                .saveChanges()
                .checkAlertMessage("Profile successfully updated")
                .checkValueInFieldName(name);
    }

    @ScreenShotTest(pathToExpFile = "img/avatar.png")
    @ApiLogin(username = MAIN_USERNAME, password = DEFAULT_PASSWORD)
    void checkAvatar(UserdataUserJson userJson, BufferedImage expected) {
        Selenide.open(ProfilePage.URL, ProfilePage.class)
                .checkAvatarCorrespondsScreenshot(expected);
    }
}

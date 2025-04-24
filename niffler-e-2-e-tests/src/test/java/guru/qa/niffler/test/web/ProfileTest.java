package guru.qa.niffler.test.web;

import com.codeborne.selenide.Selenide;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.config.Constants;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.jupiter.annotation.meta.WebTest;
import guru.qa.niffler.jupiter.annotation.Category;
import guru.qa.niffler.model.UserdataUserJson;
import guru.qa.niffler.page.LoginPage;
import guru.qa.niffler.util.RandomDataUtils;
import org.junit.jupiter.api.Test;

@WebTest
public class ProfileTest {
    private static final Config CFG = Config.getInstance();

    @Test
    @User(username = Constants.MAIN_USERNAME,
            categories = @Category(archived = true))
    void archivedCategoryShouldPresentInCategoriesList(UserdataUserJson user) {
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(Constants.MAIN_USERNAME, Constants.MAIN_PASSWORD)
                .goOnProfilePage()
                .activatedShowArchiveCategory()
                .checkNameSpendInCategoryList(
                        user.testData().categories().getFirst().name());
    }

    @Test
    @User(username = Constants.MAIN_USERNAME,
            categories = @Category(archived = false))
    void activeCategoryShouldPresentInCategoriesList(UserdataUserJson user) {
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(Constants.MAIN_USERNAME, Constants.MAIN_PASSWORD)
                .goOnProfilePage()
                .checkNameSpendInCategoryList(
                        user.testData().categories().getFirst().name());
    }

    @Test
    @User()
    void changeFieldName(UserdataUserJson userJson) {
        String name = RandomDataUtils.randomName();
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(userJson.username(), userJson.testData().password())
                .goOnProfilePage()
                .setValueInFieldName(name)
                .saveChanges()
                .checkValueInFieldName(name);
    }

}

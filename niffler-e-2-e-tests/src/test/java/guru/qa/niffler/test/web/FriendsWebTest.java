package guru.qa.niffler.test.web;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.jupiter.annotation.meta.WebTest;
import guru.qa.niffler.model.UserdataUserJson;
import guru.qa.niffler.page.LoginPage;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Selenide.open;

@WebTest
public class FriendsWebTest {
    private static final Config CFG = Config.getInstance();

    @User(username = "wolf",
    friends = 1)
    @Test
    void friendShouldBePresentInFriendsTable(UserdataUserJson user) {
        open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .clickByUserAvatar()
                .clickByFriends()
                .checkFriendInTable(user.testData().friends().getFirst().username());
    }

    @User(username = "lion")
    @Test
    void friendTableShouldBeEmptyForNewUser(UserdataUserJson user) {
        open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .clickByUserAvatar()
                .clickByFriends()
                .checkSelectedTabByName("Friends")
                .checkEmptyTable();
    }

    @User(username = "circus",
    incomeInvitations = 1)
    @Test
    void incomeInvitationBePresentInFriendsTable(UserdataUserJson user) {
        open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .clickByUserAvatar()
                .clickByFriends()
                .checkSelectedTabByName("Friends")
                .checkIncomeInTable(user.testData().income().getFirst().username());
    }

    @User(username = "cat",
    outcomeInvitations = 1)
    @Test
    void outcomeInvitationBePresentInAllPeoplesTable(UserdataUserJson user) {
        open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .clickByUserAvatar()
                .clickByAllPeople()
                .checkOutcomeInTable(user.testData().outcome().getFirst().username());
    }

}

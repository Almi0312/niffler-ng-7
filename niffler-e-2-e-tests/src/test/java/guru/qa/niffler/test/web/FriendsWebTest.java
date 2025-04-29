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

    @User(username = "wolf_with_friends",
            friends = 1)
    @Test
    void friendShouldBePresentInFriendsTable(UserdataUserJson user) {
        open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .getHeader()
                .toFriendsPage()
                .checkFriendInTable(user.testData().friends().getFirst().username());
    }

    @User(username = "lion_empty")
    @Test
    void friendTableShouldBeEmptyForNewUser(UserdataUserJson user) {
        open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .getHeader()
                .toFriendsPage()
                .checkSelectedTabByName("Friends")
                .checkEmptyTable();
    }

    @User(username = "circus_with_income",
            incomeInvitations = 1)
    @Test
    void incomeInvitationBePresentInFriendsTable(UserdataUserJson user) {
        open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .getHeader()
                .toFriendsPage()
                .checkSelectedTabByName("Friends")
                .checkIncomeInTable(user.testData().income().getFirst().username());
    }

    @User(username = "cat_with_outcome",
            outcomeInvitations = 1)
    @Test
    void outcomeInvitationBePresentInAllPeoplesTable(UserdataUserJson user) {
        open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .getHeader()
                .toAllPeoplePage()
                .checkOutcomeInTable(user.testData().outcome().getFirst().username());
    }

    @User(incomeInvitations = 1)
    @Test
    void AcceptOutcomeInvitation(UserdataUserJson user) {
        String incomeUsername = user.testData().income().getFirst().username();
        open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .getHeader()
                .toFriendsPage()
                .checkSelectedTabByName("Friends")
                .checkIncomeInTable(incomeUsername)
                .acceptIncomeInvication(incomeUsername)
                .checkFriendInTable(incomeUsername);
    }

    @User(incomeInvitations = 1)
    @Test
    void DeclineOutcomeInvitation(UserdataUserJson user) {
        String incomeUsername = user.testData().income().getFirst().username();
        open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .getHeader()
                .toFriendsPage()
                .checkSelectedTabByName("Friends")
                .checkIncomeInTable(incomeUsername)
                .declineIncomeInvication(incomeUsername)
                .checkNotFriendInTable(incomeUsername);
    }

}

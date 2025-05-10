package guru.qa.niffler.test.web;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.jupiter.annotation.ApiLogin;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.jupiter.annotation.meta.WebTest;
import guru.qa.niffler.model.rest.UserdataUserJson;
import guru.qa.niffler.page.profileInfo.AllPeoplePage;
import guru.qa.niffler.page.profileInfo.FriendsPage;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Selenide.open;
import static guru.qa.niffler.config.Constants.DEFAULT_PASSWORD;

@WebTest
public class FriendsWebTest {
    private static final Config CFG = Config.getInstance();

    @Test
    @ApiLogin
    @User(username = "wolf_with_friends",
            friends = 1)
    void friendShouldBePresentInFriendsTable(UserdataUserJson user) {
        open(FriendsPage.URL, FriendsPage.class)
                .checkFriendInTable(user.testData().friends().getFirst().username());
    }

    @Test
    @ApiLogin(username = "lion_empty", password = DEFAULT_PASSWORD)
    void friendTableShouldBeEmptyForNewUser(UserdataUserJson user) {
        open(FriendsPage.URL, FriendsPage.class)
                .checkSelectedTabByName("Friends")
                .checkEmptyTable();
    }

    @Test
    @ApiLogin
    @User(username = "circus_with_income",
            incomeInvitations = 1)
    void incomeInvitationBePresentInFriendsTable(UserdataUserJson user) {
        open(FriendsPage.URL, FriendsPage.class)
                .checkSelectedTabByName("Friends")
                .checkIncomeInTable(user.testData().income().getFirst().username());
    }

    @Test
    @ApiLogin
    @User(username = "cat_with_outcome",
            outcomeInvitations = 1)
    void outcomeInvitationBePresentInAllPeoplesTable(UserdataUserJson user) {
        open(AllPeoplePage.URL, AllPeoplePage.class)
                .checkOutcomeInTable(user.testData().outcome().getFirst().username());
    }

    @Test
    @ApiLogin
    @User(incomeInvitations = 1)
    void AcceptOutcomeInvitation(UserdataUserJson user) {
        String incomeUsername = user.testData().income().getFirst().username();
        open(FriendsPage.URL, FriendsPage.class)
                .checkSelectedTabByName("Friends")
                .checkIncomeInTable(incomeUsername)
                .acceptIncomeInvication(incomeUsername)
                .checkFriendInTable(incomeUsername);
    }

    @Test
    @ApiLogin
    @User(incomeInvitations = 1)
    void DeclineOutcomeInvitation(UserdataUserJson user) {
        String incomeUsername = user.testData().income().getFirst().username();
        open(FriendsPage.URL, FriendsPage.class)
                .checkSelectedTabByName("Friends")
                .checkIncomeInTable(incomeUsername)
                .declineIncomeInvication(incomeUsername)
                .checkNotFriendInTable(incomeUsername);
    }

}

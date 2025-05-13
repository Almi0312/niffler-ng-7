package guru.qa.niffler.test.fake.rest;

import guru.qa.niffler.jupiter.annotation.ApiLogin;
import guru.qa.niffler.jupiter.annotation.Token;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.jupiter.extension.ApiLoginExtension;
import guru.qa.niffler.model.rest.UserdataUserJson;
import guru.qa.niffler.service.GatewayClient;
import guru.qa.niffler.service.gateway.GatewayApiClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;

public class FriendsRestTest {

    @RegisterExtension
    private static final ApiLoginExtension apiLoginExtension = ApiLoginExtension.api();

    private final GatewayClient gatewayApiClient = new GatewayApiClient();

    @ApiLogin
    @User(friends = 1, incomeInvitations = 1)
    @Test
    void friendsAndIncomeInvitationsListShouldBeReturned(UserdataUserJson user, @Token String token) {
        final UserdataUserJson expectedFriend = user.testData().friends().getFirst();
        final UserdataUserJson expectedInvitation = user.testData().income().getFirst();

        final List<UserdataUserJson> response = gatewayApiClient.allFriends(token, null);

        Assertions.assertEquals(2, response.size());

        final UserdataUserJson actualInvitation = response.getFirst();
        final UserdataUserJson actualFriend = response.getLast();

        Assertions.assertEquals(expectedFriend.id(), actualFriend.id());
        Assertions.assertEquals(expectedInvitation.id(), actualInvitation.id());
    }
}

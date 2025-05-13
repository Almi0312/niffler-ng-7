package guru.qa.niffler.test.fake.rest;

import guru.qa.niffler.jupiter.annotation.ApiLogin;
import guru.qa.niffler.jupiter.annotation.Token;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.jupiter.annotation.meta.RestTest;
import guru.qa.niffler.jupiter.extension.ApiLoginExtension;
import guru.qa.niffler.model.pageable.RestResponsePage;
import guru.qa.niffler.model.rest.UserdataUserJson;
import guru.qa.niffler.service.UsersClient;
import guru.qa.niffler.service.gateway.GatewayV2ApiClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@RestTest
public class FriendsV2RestTest {
@RegisterExtension
private static final ApiLoginExtension apiLoginExtension = ApiLoginExtension.api();

private final GatewayV2ApiClient gatewayApiClient = new GatewayV2ApiClient();

@ApiLogin
@User(friends = 1, incomeInvitations = 1)
@Test
void friendsAndIncomeInvitationsListShouldBeReturned(UserdataUserJson user, @Token String token) {
    final UserdataUserJson expectedFriend = user.testData().friends().getFirst();
    final UserdataUserJson expectedInvitation = user.testData().income().getFirst();

    final RestResponsePage<UserdataUserJson> response = gatewayApiClient.allFriends(token, 0, 2, null, null);

    Assertions.assertEquals(2, response.getContent().size());

    final UserdataUserJson actualInvitation = response.getContent().getFirst();
    final UserdataUserJson actualFriend = response.getContent().getLast();

    Assertions.assertEquals(expectedFriend.id(), actualFriend.id());
    Assertions.assertEquals(expectedInvitation.id(), actualInvitation.id());
}
}

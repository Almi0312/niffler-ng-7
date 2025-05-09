package guru.qa.niffler.test.fake;

import guru.qa.niffler.jupiter.annotation.ApiLogin;
import guru.qa.niffler.jupiter.annotation.Token;
import guru.qa.niffler.model.rest.UserdataUserJson;
import guru.qa.niffler.service.AuthClient;
import guru.qa.niffler.service.auth.AuthApiClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class OAuthTest {
    private AuthClient authClient = new AuthApiClient();

    @Test
    @ApiLogin(username = "admin", password = "admin")
    void oauthTest(@Token String token, UserdataUserJson userJson) {
        System.out.println(token + "____________________________");
        System.out.println(userJson + "____________________________");
        assertNotNull(token);
    }
}

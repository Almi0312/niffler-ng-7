package guru.qa.niffler.test;

import guru.qa.niffler.service.AuthClient;
import guru.qa.niffler.service.auth.AuthApiClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class OAuthTest {
    private AuthClient authClient = new AuthApiClient();

    @Test
    void oauthTest() {
        String token = authClient.login("admin", "admin");
        assertNotNull(token);
    }
}

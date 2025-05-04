package guru.qa.niffler.service.auth;

import com.fasterxml.jackson.databind.JsonNode;
import guru.qa.niffler.api.AuthApi;
import guru.qa.niffler.api.core.RestClient;
import guru.qa.niffler.api.core.ThreadSafeCookieStore;
import guru.qa.niffler.service.AuthClient;
import guru.qa.niffler.util.OauthUtils;
import retrofit2.Response;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AuthApiClient extends RestClient implements AuthClient {

    private static final String defaultAuthScope = "openid";
    private static final String defaultAuthClientID = "client";
    private static final String defaultAuthGrantType = "authorization_code";
    private static final String defaultAuthRedirectUri = CFG.frontUrl() + "authorized";
    private static final String defaultAuthResponseType = "code";
    private static final String defaultAuthCodeChallengeMethod = "S256";

    private final AuthApi authApi;
    private String codeVerified;

    public AuthApiClient() {
        super(CFG.authUrl(), true);
        this.authApi = create(AuthApi.class);
    }

    @Override
    public void preRequestOAuthFlow() {
        final Response<Void> response;
        try {
            codeVerified = OauthUtils.generateCodeVerifier();
            response = authApi.authorize(
                            defaultAuthResponseType,
                            defaultAuthClientID,
                            defaultAuthScope,
                            defaultAuthRedirectUri,
                            OauthUtils.generateCodeChallenge(codeVerified),
                            defaultAuthCodeChallengeMethod)
                    .execute();
        } catch (IOException e) {
            throw new AssertionError(e.getMessage());
        }
        assertEquals(200, response.code(), response.message());
    }

    @Override
    public String getToken(String code) {
        final Response<JsonNode> response;
        try {
            response = authApi.token(
                    defaultAuthClientID,
                    defaultAuthRedirectUri,
                    defaultAuthGrantType,
                    code,
                    codeVerified
            ).execute();
        } catch (IOException e) {
            throw new AssertionError(e.getMessage());
        }
        assertEquals(200, response.code(), response.message());
        if(response.body() == null) {
            throw new NullPointerException("Токен не был возвращен");
        }
        return response.body().get("id_token").asText();
    }

    @Override
    public String login(String username, String password) {
        preRequestOAuthFlow();
        Response<Void> response;
        try {
            response = authApi.login().execute();
            if (response.isSuccessful()) {
                response = authApi.login(
                        username,
                        password,
                        ThreadSafeCookieStore.INSTANCE.cookieValue("XSRF-TOKEN")).execute();
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        assertEquals(200, response.code(), response.message());
        return getToken(response.raw().request().url().queryParameter("code"));
    }

    @Override
    public void create(String username, String password, String passwordSubmit) {
        Response<Void> response;
        try {
            response = authApi.registerForm().execute();
            if (response.isSuccessful()) {
                response = authApi.create(
                        username,
                        password,
                        passwordSubmit,
                        ThreadSafeCookieStore.INSTANCE.cookieValue("XSRF-TOKEN")).execute();
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        assertEquals(201, response.code(), response.message());
    }
}

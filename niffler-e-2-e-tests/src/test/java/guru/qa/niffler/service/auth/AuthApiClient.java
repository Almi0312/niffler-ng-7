package guru.qa.niffler.service.auth;

import com.fasterxml.jackson.databind.JsonNode;
import guru.qa.niffler.api.AuthApi;
import guru.qa.niffler.api.core.CodeInterceptor;
import guru.qa.niffler.api.core.RestClient;
import guru.qa.niffler.api.core.ThreadSafeCookieStore;
import guru.qa.niffler.jupiter.extension.ApiLoginExtension;
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

    public AuthApiClient() {
        super(CFG.authUrl(), true, new CodeInterceptor());
        this.authApi = create(AuthApi.class);
    }

    @Override
    public String login(String username, String password) {
        final String codeVerified = OauthUtils.generateCodeVerifier();
        final Response<JsonNode> responseToken;
        Response<Void> response;
        try {
            response = authApi.authorize(
                            defaultAuthResponseType,
                            defaultAuthClientID,
                            defaultAuthScope,
                            defaultAuthRedirectUri,
                            OauthUtils.generateCodeChallenge(codeVerified),
                            defaultAuthCodeChallengeMethod)
                    .execute();
            assertEquals(200, response.code(), response.message());

            response = authApi.login().execute();
            if (response.isSuccessful()) {
                response = authApi.login(
                        username,
                        password,
                        ThreadSafeCookieStore.INSTANCE.cookieValue("XSRF-TOKEN")).execute();
            }
            assertEquals(200, response.code(), response.message());

            responseToken = authApi.token(
                    defaultAuthClientID,
                    defaultAuthRedirectUri,
                    defaultAuthGrantType,
                    ApiLoginExtension.getCode(),
                    codeVerified
            ).execute();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        assertEquals(200, responseToken.code(), responseToken.message());
        if (responseToken.body() == null) {
            throw new NullPointerException("Токен не был возвращен");
        }
        return responseToken.body().get("id_token").asText();
    }

    @Override
    public void create(String username, String password, String passwordSubmit) {
        Response<Void> response;
        try {
            response = authApi.registerForm().execute();
            if (response.isSuccessful()) {
                authApi.create(
                        username,
                        password,
                        passwordSubmit,
                        ThreadSafeCookieStore.INSTANCE.cookieValue("XSRF-TOKEN")).execute();
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}

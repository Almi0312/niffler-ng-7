package guru.qa.niffler.service;

import com.fasterxml.jackson.databind.JsonNode;

public interface AuthClient {
    void preRequestOAuthFlow();

    String getToken(String code);

    String login(String username, String password);

    void create(String username, String password, String passwordSubmit);
}

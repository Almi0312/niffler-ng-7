package guru.qa.niffler.service;

public interface AuthClient {
    void preRequestOAuthFlow(String codeVerified);

    String getToken(String code, String codeVerified);

    String login(String username, String password);

    void create(String username, String password, String passwordSubmit);
}

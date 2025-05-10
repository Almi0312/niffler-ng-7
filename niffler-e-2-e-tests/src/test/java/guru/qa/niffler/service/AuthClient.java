package guru.qa.niffler.service;

public interface AuthClient {
    String login(String username, String password);

    void create(String username, String password, String passwordSubmit);
}

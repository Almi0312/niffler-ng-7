package guru.qa.niffler.api;

import guru.qa.niffler.model.UserdataUserJson;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import retrofit2.Response;

import javax.annotation.ParametersAreNullableByDefault;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static guru.qa.niffler.api.ApiClient.USERDATA_API;
import static java.lang.String.format;

@ParametersAreNullableByDefault
public class UserdataApiClient {

    private final UserdataApi userdataApi = USERDATA_API.getINSTANCE().create(UserdataApi.class);

    public @Nullable UserdataUserJson updateUser(UserdataUserJson user) {
        final Response<UserdataUserJson> response;
        try {
            response = userdataApi.updateUser(user).execute();
        } catch (IOException e) {
            throw new AssertionError(e.getMessage());
        }
        Assertions.assertEquals(200, response.code(), format("Юзер не был обновлен - %s", response.message()));
        return response.body();
    }

    public @Nullable UserdataUserJson getCurrentUserInfo(String username) {
        final Response<UserdataUserJson> response;
        try {
            response = userdataApi.getCurrentUserInfo(username).execute();
        } catch (IOException e) {
            throw new AssertionError(e.getMessage());
        }
        Assertions.assertEquals(200, response.code(), response.message());
        return response.body();
    }

    public @Nullable List<UserdataUserJson> getAllUsers(String username, @Nullable String searchQuery) {
        final Response<List<UserdataUserJson>> response;
        try {
            response = userdataApi.getAllUsers(username, searchQuery).execute();
        } catch (IOException e) {
            throw new AssertionError(e.getMessage());
        }
        Assertions.assertEquals(200, response.code(), "Юзеры не найдены - " + response.message());
        return response.body() != null
                ? response.body()
                : Collections.emptyList();
    }

    public @Nullable UserdataUserJson sendInvitation(String username, String targetUser) {
        final Response<UserdataUserJson> response;
        try {
            response = userdataApi.sendInvitation(username, targetUser).execute();
        } catch (IOException e) {
            throw new AssertionError(e.getMessage());
        }
        Assertions.assertEquals(200, response.code(), response.message());
        return response.body();
    }

    public @Nullable UserdataUserJson declineInvitation(String username, String targetUser) {
        final Response<UserdataUserJson> response;
        try {
            response = userdataApi.declineInvitation(username, targetUser).execute();
        } catch (IOException e) {
            throw new AssertionError(e.getMessage());
        }
        Assertions.assertEquals(200, response.code(), response.message());
        return response.body();
    }

    public @Nullable UserdataUserJson acceptInvitation(String username, String targetUser) {
        final Response<UserdataUserJson> response;
        try {
            response = userdataApi.acceptInvitation(username, targetUser).execute();
        } catch (IOException e) {
            throw new AssertionError(e.getMessage());
        }
        Assertions.assertEquals(200, response.code(), response.message());
        return response.body();
    }


    public @Nullable List<UserdataUserJson> getAllFriends(String username, @Nullable String searchQuery) {
        final Response<List<UserdataUserJson>> response;
        try {
            response = userdataApi.getAllFriends(username, searchQuery).execute();
        } catch (IOException e) {
            throw new AssertionError(e.getMessage());
        }
        Assertions.assertEquals(200, response.code(), response.message());
        return response.body() != null
                ? response.body()
                : Collections.emptyList();
    }

    public void removeFriend(String username, @Nullable String targetUsername) {
        final Response<Void> response;
        try {
            response = userdataApi.removeFriend(username, targetUsername).execute();
        } catch (IOException e) {
            throw new AssertionError(e.getMessage());
        }
        Assertions.assertEquals(200, response.code(), response.message());
    }

}

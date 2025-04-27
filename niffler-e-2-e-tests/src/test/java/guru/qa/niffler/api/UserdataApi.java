package guru.qa.niffler.api;

import guru.qa.niffler.model.UserdataUserJson;
import retrofit2.Call;
import retrofit2.http.*;

import javax.annotation.Nullable;
import java.util.List;

public interface UserdataApi {
    @POST("/internal/users/update")
    Call<UserdataUserJson> updateUser(@Body UserdataUserJson user);

    @GET("/internal/users/current")
    Call<UserdataUserJson> getCurrentUserInfo(@Query("username") String username);

    @GET("/internal/users/all")
    Call<List<UserdataUserJson>> getAllUsersByUsername(@Query("username") String username,
                                                       @Query("searchQuery") @Nullable String searchQuery);

    @POST("/internal/invitations/send")
    Call<UserdataUserJson> sendInvitation(@Query("username") String username,
                                          @Query("targetUsername") @Nullable String targetUsername);

    @POST("/internal/invitations/decline")
    Call<UserdataUserJson> declineInvitation(@Query("username") String username,
                                             @Query("targetUsername") @Nullable String targetUsername);

    @POST("/internal/invitations/accept")
    Call<UserdataUserJson> acceptInvitation(@Query("username") String username,
                                            @Query("targetUsername") @Nullable String targetUsername);

    @GET("/internal/friends/all")
    Call<List<UserdataUserJson>> getAllFriendsByUsername(@Query("username") String username,
                                                         @Query("searchQuery") @Nullable String searchQuery);

    @DELETE("/internal/friends/remove")
    Call<Void> removeFriend(@Query("username") String username,
                            @Query("targetUsername") @Nullable String targetUsername);
}

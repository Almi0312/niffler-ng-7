package guru.qa.niffler.api;

import guru.qa.niffler.model.UserdataUserJson;
import retrofit2.Call;
import retrofit2.http.*;

import javax.annotation.Nullable;

public interface UserdataApi {
    @POST("api/users/update")
    Call<UserdataUserJson> updateUser(@Body UserdataUserJson user);

    @GET("api/users/current")
    Call<UserdataUserJson> getCurrentUserInfo(@Query("username") String username);

    @GET("api/users/all")
    Call<UserdataUserJson> getAllUsers(@Query("username") String username,
                                       @Query("searchQuery") @Nullable String searchQuery);

    @POST("api/invitations/send")
    Call<UserdataUserJson> sendInvitation(@Query("username") String username,
                                          @Query("targetUsername") @Nullable String targetUsername);

    @POST("api/invitations/decline")
    Call<UserdataUserJson> declineInvitation(@Query("username") String username,
                                             @Query("targetUsername") @Nullable String targetUsername);

    @POST("api/invitations/accept")
    Call<UserdataUserJson> acceptInvitation(@Query("username") String username,
                                            @Query("targetUsername") @Nullable String targetUsername);

    @GET("api/friends/all")
    Call<UserdataUserJson> getAllFriends(@Query("username") String username,
                                         @Query("searchQuery") @Nullable String searchQuery);

    @DELETE("api/friends/remove")
    Call<Void> removeFriend(@Query("username") String username,
                                        @Query("targetUsername") @Nullable String targetUsername);
}

package guru.qa.niffler.api;

import retrofit2.Call;
import retrofit2.http.*;

public interface AuthApi {

    @GET("/login")
    Call<Void> login();

    @POST("/login")
    @FormUrlEncoded
    Call<Void> login(
            @Field("username") String username,
            @Field("password") String password,
            @Field("_csrf") String csrf);

    @GET("/register")
    Call<Void> registerForm();

    @POST("/register")
    @FormUrlEncoded
    Call<Void> create(
            @Field("username") String username,
            @Field("password") String password,
            @Field("passwordSubmit") String passwordSubmit,
            @Field("_csrf") String csrf);
}

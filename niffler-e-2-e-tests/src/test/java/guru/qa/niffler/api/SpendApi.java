package guru.qa.niffler.api;

import guru.qa.niffler.model.rest.CategoryJson;
import guru.qa.niffler.model.CurrencyValues;
import guru.qa.niffler.model.rest.SpendJson;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface SpendApi {

    @POST("internal/spends/add")
    Call<SpendJson> addSpend(@Body SpendJson spend);

    @PATCH("internal/spends/edit")
    Call<SpendJson> editSpend(@Body SpendJson spend);

    @GET("internal/spends/{id}")
    Call<SpendJson> getIDSpend(@Query("id") String id);

    @GET("internal/spends/all")
    Call<List<SpendJson>> getAllSpend(@Query("username") String username,
                                      @Query("filterCurrency") CurrencyValues filterCurrency,
                                      @Query("from") String from,
                                      @Query("to") String to);

    @DELETE("internal/spends/remove")
    Call<Void> removeSpend(@Query("username") String username,
                           @Query("ids") List<String> ids);

    @POST("internal/categories/add")
    Call<CategoryJson> addCategory(@Body CategoryJson spend);

    @PATCH("internal/categories/update")
    Call<CategoryJson> updateCategories(@Body CategoryJson spend);

    @GET("internal/categories/all")
    Call<List<CategoryJson>> allCategories(@Query("username") String username,
                                           @Query("excludeArchived") Boolean excludeArchived);

}

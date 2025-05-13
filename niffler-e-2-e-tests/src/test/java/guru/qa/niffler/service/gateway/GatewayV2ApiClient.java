package guru.qa.niffler.service.gateway;

import guru.qa.niffler.api.GatewayV2Api;
import guru.qa.niffler.api.core.RestClient;
import guru.qa.niffler.model.pageable.RestResponsePage;
import guru.qa.niffler.model.rest.UserdataUserJson;
import guru.qa.niffler.service.UsersClient;
import guru.qa.niffler.service.userdata.GatewayV2Client;
import io.qameta.allure.Step;
import retrofit2.Response;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GatewayV2ApiClient extends RestClient implements GatewayV2Client {

    private final GatewayV2Api gatewayApi;

    public GatewayV2ApiClient() {
        super(CFG.gatewayUrl());
        this.gatewayApi = create(GatewayV2Api.class);
    }

    @Step("Send GET request /api/v2/friends/all to niffler-gateway")
    @Nonnull
    public RestResponsePage<UserdataUserJson> allFriends(String bearerToken,
                                                              int page,
                                                              int size,
                                                              @Nullable String sort,
                                                              @Nullable String searchQuery) {
        final Response<RestResponsePage<UserdataUserJson>> response;
        try {
            response = gatewayApi.allFriends(bearerToken,
                            page,
                            size,
                            sort,
                            searchQuery)
                    .execute();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        assertEquals(200, response.code());
        return requireNonNull(response.body());
    }
}

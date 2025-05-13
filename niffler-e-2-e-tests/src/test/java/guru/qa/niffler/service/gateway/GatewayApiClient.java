package guru.qa.niffler.service.gateway;

import guru.qa.niffler.api.GatewayApi;
import guru.qa.niffler.api.core.RestClient;
import guru.qa.niffler.model.rest.UserdataUserJson;
import guru.qa.niffler.service.GatewayClient;
import io.qameta.allure.Step;
import retrofit2.Response;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GatewayApiClient extends RestClient implements GatewayClient {

    private final GatewayApi gatewayApi;

    public GatewayApiClient() {
        super(CFG.gatewayUrl());
        this.gatewayApi = create(GatewayApi.class);
    }

    @Step("Send GET request /api/friends/all to niffler-gateway")
    @Nonnull
    public List<UserdataUserJson> allFriends(String bearerToken,
                                                  @Nullable String searchQuery) {
        final Response<List<UserdataUserJson>> response;
        try {
            response = gatewayApi.allFriends(bearerToken, searchQuery)
                    .execute();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        assertEquals(200, response.code());
        return requireNonNull(response.body());
    }
}

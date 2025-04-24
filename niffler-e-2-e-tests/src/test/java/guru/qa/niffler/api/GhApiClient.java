package guru.qa.niffler.api;

import com.fasterxml.jackson.databind.JsonNode;
import guru.qa.niffler.api.core.RestClient;
import retrofit2.Response;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.Objects;

import static guru.qa.niffler.api.ApiClient.GH_API;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ParametersAreNonnullByDefault
public class GhApiClient extends RestClient {
    private final GhApi ghApi;

    private static final String GH_TOKEN_ENV = "Niffler7";

    public GhApiClient() {
        super(CFG.ghUrl());
        this.ghApi = GH_API.getINSTANCE().create(GhApi.class);
    }

    public @Nonnull String issueState(@Nonnull String issueNumber) {
        final Response<JsonNode> response;
        try {
            response = ghApi.issue("Bearer " + System.getenv(GH_TOKEN_ENV),
                    issueNumber
            ).execute();
        } catch (IOException e) {
            throw new AssertionError(e.getMessage());
        }
        assertEquals(200, response.code(), "Получить траты не удалось: " + response.message());
        return Objects.requireNonNull(response.body()).get("state").asText();
    }
}

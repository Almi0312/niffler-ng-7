package guru.qa.niffler.api;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.SneakyThrows;
import retrofit2.Response;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.Objects;

import static guru.qa.niffler.api.ApiClient.GH_API;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ParametersAreNonnullByDefault
public class GhApiClient {
    private final GhApi ghApi = GH_API.getINSTANCE().create(GhApi.class);

    private static final String GH_TOKEN_ENV = "Niffler7";

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

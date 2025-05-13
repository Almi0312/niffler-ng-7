package guru.qa.niffler.service;

import guru.qa.niffler.model.rest.UserdataUserJson;

import javax.annotation.Nullable;
import java.util.List;

public interface GatewayClient {
    List<UserdataUserJson> allFriends(String bearerToken,
                                      @Nullable String searchQuery);
}

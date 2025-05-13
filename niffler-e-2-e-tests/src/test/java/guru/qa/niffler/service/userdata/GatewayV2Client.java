package guru.qa.niffler.service.userdata;

import guru.qa.niffler.model.pageable.RestResponsePage;
import guru.qa.niffler.model.rest.UserdataUserJson;

import javax.annotation.Nullable;
import java.util.List;

public interface GatewayV2Client {
    RestResponsePage<UserdataUserJson> allFriends(String bearerToken,
                                                  int page,
                                                  int size,
                                                  @Nullable String sort,
                                                  @Nullable String searchQuery);
}

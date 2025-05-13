package guru.qa.niffler.jupiter.extension;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import guru.qa.niffler.api.core.ThreadSafeCookieStore;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.jupiter.annotation.ApiLogin;
import guru.qa.niffler.jupiter.annotation.Token;
import guru.qa.niffler.model.FriendshipStatus;
import guru.qa.niffler.model.TestData;
import guru.qa.niffler.model.rest.UserdataUserJson;
import guru.qa.niffler.page.MainPage;
import guru.qa.niffler.service.AuthClient;
import guru.qa.niffler.service.SpendsClient;
import guru.qa.niffler.service.UsersClient;
import guru.qa.niffler.service.auth.AuthApiClient;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.support.AnnotationSupport;
import org.openqa.selenium.Cookie;

import java.util.List;

public class ApiLoginExtension implements BeforeEachCallback, ParameterResolver {

    public static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(ApiLoginExtension.class);
    private static final Config CFG = Config.getInstance();
    private static final String sessionName = "JSESSIONID";

    private final AuthClient authApiClient = new AuthApiClient();
    private final SpendsClient spendsClient = SpendsClient.getInstance();
    private final UsersClient usersClient = UsersClient.getInstance();
    private final boolean setupBrowser;

    private ApiLoginExtension(boolean setupBrowser) {
        this.setupBrowser = setupBrowser;
    }

    public ApiLoginExtension() {
        this.setupBrowser = true;
    }

    public static ApiLoginExtension api() {
        return new ApiLoginExtension(false);
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        AnnotationSupport.findAnnotation(context.getRequiredTestMethod(), ApiLogin.class)
                .ifPresent(apiLogin -> {
                    final UserdataUserJson userToLogin;
                    final UserdataUserJson userFromUserExtension = UserExtension.createdUser();
                    if ("".equals(apiLogin.username()) || "".equals(apiLogin.password())) {
                        if (userFromUserExtension == null) {
                            throw new IllegalStateException("@User must be present in case that @ApiLogin is empty!");
                        }
                        userToLogin = userFromUserExtension;
                    } else {
                        if (userFromUserExtension != null) {
                            throw new IllegalStateException("@User must not be present in case that @ApiLogin contains username or password!");
                        }
                        List<UserdataUserJson> friendshipUsers = usersClient
                                .findAllFriendshipByUsername(apiLogin.username(), "");
                        UserdataUserJson fakeUser = new UserdataUserJson(
                                apiLogin.username(),
                                new TestData(
                                        apiLogin.password(),
                                        spendsClient.findAllCategoryByUsername(apiLogin.username()),
                                        spendsClient.findAllByUsername(apiLogin.username()),
                                        getFriendshipByStatus(friendshipUsers, FriendshipStatus.INVITE_RECEIVED),
                                        getFriendshipByStatus(friendshipUsers, FriendshipStatus.INVITE_SENT),
                                        getFriendshipByStatus(friendshipUsers, FriendshipStatus.FRIEND)
                                )
                        );
                        UserExtension.setUser(fakeUser);
                        userToLogin = fakeUser;
                    }
                    setToken(authApiClient.login(
                            userToLogin.username(),
                            userToLogin.testData().password()
                    ));
                    if (setupBrowser) {
                        Selenide.open(CFG.frontUrl());
                        Selenide.localStorage().setItem("id_token", getToken());
                        WebDriverRunner.getWebDriver().manage().addCookie(
                                getJsessionIdCookie()
                        );
                        Selenide.open(MainPage.URL, MainPage.class).checkThatPageLoaded();
                    }
                });
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().isAssignableFrom(String.class)
                && AnnotationSupport.isAnnotated(parameterContext.getParameter(), Token.class);
    }

    @Override
    public String resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return "Bearer " + getToken();
    }

    public static void setToken(String token) {
        TestMethodContextExtension.context().getStore(NAMESPACE).put("token", token);
    }

    public static String getToken() {
        return TestMethodContextExtension.context().getStore(NAMESPACE).get("token", String.class);
    }

    public static void setCode(String code) {
        TestMethodContextExtension.context().getStore(NAMESPACE).put("code", code);
    }

    public static String getCode() {
        return TestMethodContextExtension.context().getStore(NAMESPACE).get("code", String.class);
    }

    public static Cookie getJsessionIdCookie() {
        return new Cookie(sessionName,
                ThreadSafeCookieStore.INSTANCE.cookieValue(sessionName));
    }

    private List<UserdataUserJson> getFriendshipByStatus(List<UserdataUserJson> users, FriendshipStatus status) {
        return users.stream().filter(user -> user.friendshipStatus().equals(status)).toList();
    }
}
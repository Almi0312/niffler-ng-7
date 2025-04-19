package guru.qa.niffler.jupiter.extension;


import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.model.CurrencyValues;
import guru.qa.niffler.model.TestData;
import guru.qa.niffler.model.UserdataUserJson;
import guru.qa.niffler.service.userdata.UserdataDBSpringRepositoryClient;
import guru.qa.niffler.service.userdata.UserdataHibernateDBClient;
import guru.qa.niffler.service.userdata.UsersClient;
import guru.qa.niffler.util.RandomDataUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.support.AnnotationSupport;

import java.util.ArrayList;

@Slf4j
public class UserExtension implements BeforeEachCallback,
        ParameterResolver {

    public static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(UserExtension.class);
    private static final String defaultPassword = "12345";

    private final UsersClient usersClient = new UserdataDBSpringRepositoryClient();

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        AnnotationSupport.findAnnotation(context.getRequiredTestMethod(), User.class)
                .ifPresent(userAnno -> {
                    String username = "".equals(userAnno.username()) ?
                            RandomDataUtils.randomUsername()
                            : userAnno.username();
                    UserdataUserJson userJson = usersClient.create(username, CurrencyValues.RUB, defaultPassword);
                    context.getStore(NAMESPACE).put(
                            context.getUniqueId(),
                            userJson.addTestData(
                                    new TestData(
                                            defaultPassword,
                                            new ArrayList<>(),
                                            new ArrayList<>())));
                });
        log.info("создан пользователь - {}", context.getStore(NAMESPACE). get(context.getUniqueId(), UserdataUserJson.class));
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().isAssignableFrom(UserdataUserJson.class);
    }

    @Override
    public UserdataUserJson resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return extensionContext.getStore(NAMESPACE).get(
                extensionContext.getUniqueId(),
                UserdataUserJson.class);
    }
}

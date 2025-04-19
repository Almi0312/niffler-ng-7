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
import java.util.Arrays;
import java.util.List;

@Slf4j
public class UserExtension implements BeforeEachCallback,
        ParameterResolver {

    public static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(UserExtension.class);
    private static final String defaultPassword = "pass";

    private final UsersClient usersClient = new UserdataDBSpringRepositoryClient();

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        AnnotationSupport.findAnnotation(context.getRequiredTestMethod(), User.class)
                .ifPresent(userAnno -> {
                    String username = "".equals(userAnno.username()) ?
                            RandomDataUtils.randomUsername()
                            : userAnno.username();
                    UserdataUserJson userJson = usersClient.create(username, CurrencyValues.RUB, defaultPassword);
                    String[] incomes = getArrayWithRandomUsername(userAnno.incomeInvitations());
                    String[] outcomes = getArrayWithRandomUsername(userAnno.outcomeInvitations());
                    String[] friends = getArrayWithRandomUsername(userAnno.friends());
                    usersClient.createIncomeInvitations(userJson, incomes);
                    usersClient.createOutcomeInvitations(userJson, outcomes);
                    usersClient.createFriends(userJson, friends);
                    context.getStore(NAMESPACE).put(
                            context.getUniqueId(),
                            userJson.addTestData(
                                    new TestData(
                                            defaultPassword,
                                            new ArrayList<>(),
                                            new ArrayList<>(),
                                            findUsersByUsername(incomes),
                                            findUsersByUsername(outcomes),
                                            findUsersByUsername(friends)
                                            )));
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

    private List<UserdataUserJson> findUsersByUsername(String[] usernames) {
        return Arrays.stream(usernames)
                .map(x -> usersClient.findByUsername(x).orElse(null))
                .toList();
    }

    private String[] getArrayWithRandomUsername(int x) {
        String[] usernames = new String[x];
        for (int i = 0; i < x; i++) {
            usernames[i] = RandomDataUtils.randomUsername();
        }
        return usernames;
    }

}

package guru.qa.niffler.jupiter.extension;

import guru.qa.niffler.jupiter.annotation.Spending;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.model.SpendJson;
import guru.qa.niffler.model.UserdataUserJson;
import guru.qa.niffler.service.spend.SpendDBSpringRepositoryClient;
import guru.qa.niffler.service.spend.SpendsClient;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.support.AnnotationSupport;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SpendingExtension implements BeforeEachCallback,
        ParameterResolver
//        AfterEachCallback
{

    public static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace
            .create(SpendingExtension.class);

    private final SpendsClient spendClient = new SpendDBSpringRepositoryClient();

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        AnnotationSupport.findAnnotation(context.getRequiredTestMethod(), User.class)
                .ifPresent(userAnno -> {
                    UserdataUserJson userJson = context.getStore(UserExtension.NAMESPACE)
                            .get(context.getUniqueId(), UserdataUserJson.class);
                    final String username = userJson != null
                            ? userJson.username()
                            : userAnno.username();
                    final List<SpendJson> createdSpends = new ArrayList<>();
                    for (Spending annoSpend : userAnno.spendings()) {
                        SpendJson spend = new SpendJson(
                                null,
                                new Date(),
                                spendClient.findCategoryByUsernameAndName(
                                                username, annoSpend.category())
                                        .orElse(
                                                new CategoryJson(
                                                        null,
                                                        annoSpend.category(),
                                                        username,
                                                        false)),
                                annoSpend.currency(),
                                annoSpend.amount(),
                                annoSpend.description(),
                                username
                        );
                        createdSpends.add(spendClient.createSpend(spend));
                    }
                    if (userJson != null) {
                        userJson.testData().spendings().addAll(createdSpends);
                    } else {
                        context.getStore(NAMESPACE).put(
                                context.getUniqueId(),
                                createdSpends
                        );
                    }
                });
    }

//    @Override
//    public void afterEach(ExtensionContext context) throws Exception {
//        SpendJson spendJson = context.getStore(NAMESPACE).get(context.getUniqueId(), SpendJson.class);
//        if (spendJson != null) {
//            spendClient.remove(spendJson);
//        }
//    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().isAssignableFrom(SpendJson[].class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public SpendJson[] resolveParameter(ParameterContext parameterContext,
                                        ExtensionContext extensionContext) throws ParameterResolutionException {
        return (SpendJson[]) extensionContext.getStore(SpendingExtension.NAMESPACE)
                .get(extensionContext.getUniqueId(), List.class)
                .stream()
                .toArray(SpendJson[]::new);
    }

}

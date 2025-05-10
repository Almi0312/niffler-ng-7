package guru.qa.niffler.jupiter.extension;

import guru.qa.niffler.jupiter.annotation.Spending;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.model.rest.CategoryJson;
import guru.qa.niffler.model.rest.SpendJson;
import guru.qa.niffler.model.rest.UserdataUserJson;
import guru.qa.niffler.service.SpendsClient;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.support.AnnotationSupport;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@ParametersAreNonnullByDefault
public class SpendingExtension implements BeforeEachCallback,
        ParameterResolver
//        AfterEachCallback
{

    public static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace
            .create(SpendingExtension.class);

    private final SpendsClient spendClient = SpendsClient.getInstance();

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        AnnotationSupport.findAnnotation(context.getRequiredTestMethod(), User.class)
                .ifPresent(userAnno -> {
                    UserdataUserJson userJson = UserExtension.createdUser();
                    final String username = userJson != null
                            ? userJson.username()
                            : userAnno.username();

                    final List<CategoryJson> existingCategories = userJson != null
                            ? userJson.testData().categories()
                            : CategoryExtension.createdCategories(context);
                    final List<SpendJson> createdSpends = new ArrayList<>();
                    for (Spending annoSpend : userAnno.spendings()) {
                        final Optional<CategoryJson> matchedCategory = existingCategories.stream()
                                .filter(category -> category.name().equals(annoSpend.category()))
                                .findFirst();

                        SpendJson spend = new SpendJson(
                                null,
                                new Date(),
                                matchedCategory.orElseGet(() -> new CategoryJson(
                                        null,
                                        annoSpend.category(),
                                        username,
                                        false
                                )),
                                annoSpend.currency(),
                                annoSpend.amount(),
                                annoSpend.description(),
                                username
                        );
                        createdSpends.add(spendClient.createSpend(spend));
                    }
                    if (userJson != null) {
                        UserExtension.createdUser().testData().spendings().addAll(createdSpends);
                    } else {
                        context.getStore(NAMESPACE).put(
                                context.getUniqueId(),
                                createdSpends
                        );
                    }
                });
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().isAssignableFrom(SpendJson[].class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public SpendJson[] resolveParameter(ParameterContext parameterContext,
                                        ExtensionContext extensionContext) throws ParameterResolutionException {
        return createdSpends(extensionContext).toArray(SpendJson[]::new);
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public static List<SpendJson> createdSpends(ExtensionContext extensionContext) {
        return Optional.ofNullable(extensionContext.getStore(NAMESPACE).get(extensionContext.getUniqueId(), List.class))
                .orElse(Collections.emptyList());
    }

}

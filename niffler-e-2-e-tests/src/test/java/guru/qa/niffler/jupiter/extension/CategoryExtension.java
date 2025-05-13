package guru.qa.niffler.jupiter.extension;

import guru.qa.niffler.jupiter.annotation.Category;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.model.rest.CategoryJson;
import guru.qa.niffler.model.rest.UserdataUserJson;
import guru.qa.niffler.service.spend.SpendApiClient;
import guru.qa.niffler.service.SpendsClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.support.AnnotationSupport;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static guru.qa.niffler.model.CurrencyValues.RUB;
import static guru.qa.niffler.util.RandomDataUtils.*;

@Slf4j
@ParametersAreNonnullByDefault
public class CategoryExtension implements BeforeEachCallback,
        ParameterResolver
//        AfterTestExecutionCallback
{
    public static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace
            .create(CategoryExtension.class);

    private final SpendsClient spendClient = SpendsClient.getInstance();

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        AnnotationSupport.findAnnotation(context.getRequiredTestMethod(), User.class)
                .ifPresent(userAnno -> {
                    UserdataUserJson userJson = UserExtension.createdUser();
                    final String username = userJson != null
                            ? userJson.username()
                            : userAnno.username();
                    log.info("найден пользователь - {}", username);
                    final List<CategoryJson> createdCategories = new ArrayList<>();
                    for (Category annoCategory : userAnno.categories()) {
                        CategoryJson categoryJson = new CategoryJson(
                                null,
                                "".equals(annoCategory.name()) ? randomCategoryName() : annoCategory.name(),
                                username,
                                annoCategory.archived()
                        );
                        createdCategories.add(spendClient.createCategory(categoryJson));
                    }
                    if (userJson != null) {
                        UserExtension.createdUser().testData().categories().addAll(createdCategories);
                    } else {
                        context.getStore(NAMESPACE).put(
                                context.getUniqueId(),
                                createdCategories);
                    }
                });
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().isAssignableFrom(CategoryJson[].class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public CategoryJson[] resolveParameter(ParameterContext parameterContext,
                                           ExtensionContext extensionContext) throws ParameterResolutionException {
        return createdCategories(extensionContext).toArray(CategoryJson[]::new);
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public static List<CategoryJson> createdCategories(ExtensionContext extensionContext) {
        return Optional.ofNullable(extensionContext.getStore(NAMESPACE).get(extensionContext.getUniqueId(), List.class))
                .orElse(Collections.emptyList());
    }
}

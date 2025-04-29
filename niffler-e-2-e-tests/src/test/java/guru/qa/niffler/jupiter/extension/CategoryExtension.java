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

import java.util.ArrayList;
import java.util.List;

import static guru.qa.niffler.util.RandomDataUtils.*;

@Slf4j
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
                    UserdataUserJson userJson = context.getStore(UserExtension.NAMESPACE)
                            .get(context.getUniqueId(), UserdataUserJson.class);
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
                        userJson.testData().categories().addAll(createdCategories);
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
        return (CategoryJson[]) extensionContext.getStore(CategoryExtension.NAMESPACE)
                .get(extensionContext.getUniqueId(), List.class)
                .stream()
                .toArray(CategoryJson[]::new);
    }

//    @Override
//    public void afterTestExecution(ExtensionContext context) throws Exception {
//        CategoryJson category = context.getStore(NAMESPACE).get(context.getUniqueId(), CategoryJson.class);
//        if (category != null) {
//            categoryDBClient.delete(category);
//        }
//    }
}

package guru.qa.niffler.model;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public record TestData(String password,
                       List<CategoryJson> categories,
                       List<SpendJson> spendings,
                       List<UserdataUserJson> income,
                       List<UserdataUserJson> outcome,
                       List<UserdataUserJson> friends) {
}

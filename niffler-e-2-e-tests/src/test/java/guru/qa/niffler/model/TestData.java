package guru.qa.niffler.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
public record TestData(
        @JsonIgnore String password,
        @JsonIgnore List<CategoryJson> categories,
        @JsonIgnore List<SpendJson> spendings,
        @JsonIgnore List<UserdataUserJson> income,
        @JsonIgnore List<UserdataUserJson> outcome,
        @JsonIgnore List<UserdataUserJson> friends) {

    public TestData(String password) {
        this(password, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }
}

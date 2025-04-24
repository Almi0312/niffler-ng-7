package guru.qa.niffler.page.component;

import com.codeborne.selenide.SelenideElement;

import javax.annotation.Nonnull;
import java.util.Objects;

import static com.codeborne.selenide.Condition.exist;

public class SearchField extends BaseComponent {

    public SearchField(SelenideElement self) {
        super(self);
    }

    public SearchField search(String query) {
        clearIfNotEmpty();
        self.shouldBe(exist).setValue(query).pressEnter();
        return this;
    }

    public @Nonnull SearchField clearIfNotEmpty() {
        if (!Objects.requireNonNull(self.getValue()).isEmpty()) {
            self.clear();
        }
        return this;
    }
}

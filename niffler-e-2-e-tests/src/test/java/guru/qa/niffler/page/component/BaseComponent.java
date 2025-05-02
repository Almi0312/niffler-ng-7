package guru.qa.niffler.page.component;

import com.codeborne.selenide.SelenideElement;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Selenide.$x;
import static java.lang.String.format;

@ParametersAreNonnullByDefault
public abstract class BaseComponent<T extends BaseComponent<?>> {
    protected final SelenideElement self;

    public BaseComponent(SelenideElement self) {
        self.shouldBe(exist);
        this.self = self;
    }

    protected SelenideElement getButtonByText(String text) {
        return $x(format(".//*[normalize-space(text()) = '%s']/ancestor-or-self::button", text));
    }

    protected SelenideElement link(String text) {
        return $x(format(".//*[normalize-space(text()) = '%s']/ancestor-or-self::a", text));
    }

    public SelenideElement getElement() {
        return self;
    }
}

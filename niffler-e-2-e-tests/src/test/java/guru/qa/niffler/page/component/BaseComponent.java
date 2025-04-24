package guru.qa.niffler.page.component;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$x;
import static java.lang.String.format;

public abstract class BaseComponent {
    protected final SelenideElement self;

    public BaseComponent(SelenideElement self) {
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

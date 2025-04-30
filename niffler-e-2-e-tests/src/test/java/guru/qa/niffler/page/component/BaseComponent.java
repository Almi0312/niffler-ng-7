package guru.qa.niffler.page.component;

import com.codeborne.selenide.SelenideElement;
import guru.qa.niffler.util.ScreenDiffResult;
import guru.qa.niffler.util.SupportUtils;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$x;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

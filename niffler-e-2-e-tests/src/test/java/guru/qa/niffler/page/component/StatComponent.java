package guru.qa.niffler.page.component;


import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import guru.qa.niffler.condition.Color;
import guru.qa.niffler.util.ScreenDiffResult;
import guru.qa.niffler.util.SupportUtils;
import io.qameta.allure.Step;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.IOException;

import static com.codeborne.selenide.CollectionCondition.texts;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$x;
import static guru.qa.niffler.condition.StatConditions.statBubbles;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ParametersAreNonnullByDefault
public class StatComponent extends BaseComponent<StatComponent> {

    public StatComponent() {
        super($x(".//div[@id='stat']"));
    }

    private final SelenideElement diagram = $x(".//canvas[@role='img']");
    private final ElementsCollection bubbles = self.$x(".//div[@id='legend-container']").$$x(".//li");

    public StatComponent checkTextInBubbles(String ... texts) {
        bubbles.shouldBe(texts(texts));
        return this;
    }

    public StatComponent checkBubbles(Bubble ... bubbles) {
        this.bubbles.shouldBe(statBubbles("background-color", bubbles));
        return this;
    }

    @Step("Проверить что отображение диаграммы не изменилось от ожидаемого")
    public StatComponent checkDiagramCorrespondsScreenshot(BufferedImage expectedImage) {
        assertTrue(SupportUtils.waitResult(3, 500,
                () -> {
                    BufferedImage actual;
                    try {
                        actual = ImageIO.read(diagram.shouldBe(visible).screenshot());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return !(new ScreenDiffResult(expectedImage, actual).getAsBoolean());
                }));
        return this;
    }

    @Step("Проверить что отображение диаграммы изменилось")
    public StatComponent checkDiagramNoCorrespondsScreenshot(BufferedImage expectedImage) {
        assertTrue(SupportUtils.waitResult(3, 500,
                () -> {
                    BufferedImage actual;
                    try {
                        actual = ImageIO.read(diagram.shouldBe(visible).screenshot());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return new ScreenDiffResult(expectedImage, actual).getAsBoolean();
                }));
        return this;
    }

    public record Bubble(Color color, String text) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Bubble bubble)) return false;
            return color.equals(bubble.color) && text.equals(bubble.text);
        }

        @Override
        public int hashCode() {
            return 31 * color.hashCode() + text.hashCode();
        }

        @Override
        public String toString() {
            return "\nBubble - %s with color - %s".formatted(text, color);
        }
    }
}
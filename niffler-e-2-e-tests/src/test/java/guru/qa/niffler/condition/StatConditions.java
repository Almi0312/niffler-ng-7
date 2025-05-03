package guru.qa.niffler.condition;

import com.codeborne.selenide.CheckResult;
import com.codeborne.selenide.Driver;
import com.codeborne.selenide.WebElementCondition;
import com.codeborne.selenide.WebElementsCondition;
import guru.qa.niffler.page.component.StatComponent;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.WebElement;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ParametersAreNonnullByDefault
public class StatConditions {

    public static WebElementCondition color(String cssAttr, Color expectedColor) {
        return new WebElementCondition("color") {
            @NotNull
            @Override
            public CheckResult check(Driver driver, WebElement webElement) {
                final String rgba = webElement.getCssValue(cssAttr);
                return new CheckResult(
                        expectedColor.rgb.equals(rgba),
                        rgba);
            }
        };
    }

    public static WebElementsCondition colors(String cssAttr, Color... expectedColors) {
        return new WebElementsCondition() {
            private final String expectedRgba = Arrays.stream(expectedColors).map(x -> x.rgb).toList().toString();
            @NotNull
            @Override
            public CheckResult check(Driver driver, List<WebElement> elements) {
                checkIsNotEmpty(expectedColors);
                if (expectedColors.length != elements.size()) {
                    String message = String.format("List size mismatch (expected: %s, actual: %s",
                            expectedColors.length, elements.size());
                    return CheckResult.rejected(message, elements);
                }

                boolean passed = true;
                List<String> actualColors = new ArrayList<>();
                for (int i = 0; i < elements.size(); i++) {
                    final String rgba = elements.get(i).getCssValue(cssAttr);
                    final Color colorToCheck = expectedColors[i];
                    actualColors.add(rgba);
                    if(passed) {
                        passed = colorToCheck.rgb.equals(rgba);
                    }
                }
                if(!passed) {
                    final String actualRgba = actualColors.toString();
                    final String message = String.format("List colors mismatch (expected: %s, actual: %s",
                            expectedRgba, actualRgba);
                    return CheckResult.rejected(message, actualRgba);
                }
                return CheckResult.accepted();
            }

            @Override
            public String toString() {
                return expectedRgba;
            }
        };
    }

    public static WebElementsCondition statBubbles(String cssAttr, StatComponent.Bubble... bubbles) {
        String bubblesStr = Arrays.stream(bubbles).toList().toString();
        return new WebElementsCondition() {
            @NotNull
            @Override
            public CheckResult check(Driver driver, List<WebElement> elements) {
                checkIsNotEmpty(bubbles);
                if (bubbles.length != elements.size()) {
                    String message = String.format("List size mismatch (expected: %s, actual: %s",
                            bubbles.length, elements.size());
                    return CheckResult.rejected(message, elements);
                }
                boolean passed = true;
                List<StatComponent.Bubble> actualBubbles = getBubblesFromElement(cssAttr, elements);
                for (int i = 0; i < elements.size(); i++) {
                    if (passed) {
                        passed = bubbles[i].equals(actualBubbles.get(i));
                    } else break;
                }
                if (!passed) {
                    final String message = String.format("List bubble mismatch (expected: %s, actual: %s",
                            Arrays.toString(bubbles), actualBubbles);
                    return CheckResult.rejected(message, actualBubbles);
                }
                return CheckResult.accepted();
            }

            @Override
            public String toString() {
                return bubblesStr;
            }
        };
    }

    public static WebElementsCondition statBubblesInAnyOrder(String cssAttr, StatComponent.Bubble... bubbles) {
        String bubblesStr = Arrays.asList(bubbles).toString();
        return new WebElementsCondition() {
            @NotNull
            @Override
            public CheckResult check(Driver driver, List<WebElement> elements) {
                checkIsNotEmpty(bubbles);
                if (bubbles.length != elements.size()) {
                    String message = String.format("List size mismatch (expected: %s, actual: %s",
                            bubbles.length, elements.size());
                    return CheckResult.rejected(message, elements);
                }
                return checkContainsBubble(cssAttr, elements, bubbles);
            }

            @Override
            public String toString() {
                return bubblesStr;
            }
        };
    }

    public static WebElementsCondition statBubblesContains(String cssAttr, StatComponent.Bubble... bubbles) {
        String bubblesStr = Arrays.asList(bubbles).toString();
        return new WebElementsCondition() {
            @NotNull
            @Override
            public CheckResult check(Driver driver, List<WebElement> elements) {
                checkIsNotEmpty(bubbles);
                if (bubbles.length > elements.size()) {
                    String message = String.format("List size mismatch (expected: %s, actual: %s",
                            bubbles.length, elements.size());
                    return CheckResult.rejected(message, elements);
                }
                return checkContainsBubble(cssAttr, elements, bubbles);
            }

            @Override
            public String toString() {
                return bubblesStr;
            }
        };
    }

    /**
     * Проверяет, что переданный массив Bubbles не пуст
     **/
    private static <T> void checkIsNotEmpty(T[] array) {
        if (ArrayUtils.isEmpty(array)) {
            throw new IllegalArgumentException("No expected array given");
        }
    }

    /**
     * Возвращает коллекцию актуальных элементов Bubble со страницы
     **/
    private static List<StatComponent.Bubble> getBubblesFromElement(String cssAttr, List<WebElement> elements) {
        return elements.stream()
                .map(x -> new StatComponent.Bubble(
                        Color.fromCss(x.getCssValue(cssAttr)),
                        x.getText()))
                .toList();
    }

    /**
     * Проверяет с помощью Selenide то, что актуальные элементы Bubble содержат ожидаемые
     */
    private static CheckResult checkContainsBubble(String cssAttr, List<WebElement> elements, StatComponent.Bubble[] bubbles) {
        List<StatComponent.Bubble> actualBubbles = getBubblesFromElement(cssAttr, elements);
        if (!actualBubbles.containsAll(Arrays.asList(bubbles))) {
            return CheckResult.rejected(
                    String.format("List bubble mismatch (expected: %s, actual: %s",
                            Arrays.asList(bubbles), actualBubbles),
                    actualBubbles.toString());
        }
        return CheckResult.accepted();
    }
}

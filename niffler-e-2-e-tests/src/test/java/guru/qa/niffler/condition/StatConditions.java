package guru.qa.niffler.condition;

import com.codeborne.selenide.CheckResult;
import com.codeborne.selenide.Driver;
import com.codeborne.selenide.WebElementCondition;
import com.codeborne.selenide.WebElementsCondition;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
                if (ArrayUtils.isEmpty(expectedColors)) {
                    throw new IllegalArgumentException("No expected colors given");
                }
                if (expectedColors.length != elements.size()) {
                    String message = String.format("List size mismatch (expected: %s, actual: %s",
                            expectedColors.length, elements.size());
                    return CheckResult.rejected(message, elements);
                }

                boolean passed = true;
                List<String> actualColors = new ArrayList<>();
                for (int i = 0; i < elements.size(); i++) {
                    final WebElement elementToCheck = elements.get(i);
                    final Color colorToCheck = expectedColors[i];
                    final String rgba = elementToCheck.getCssValue(cssAttr);
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
}

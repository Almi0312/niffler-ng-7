package guru.qa.niffler.util;

import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.function.Supplier;

import static com.codeborne.selenide.WebDriverRunner.driver;
import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;

public class SupportUtils {

    public static boolean waitResult(int waitSec, int polling, Supplier<Boolean> supplier) {
        try {
            new WebDriverWait(driver().getWebDriver(), ofSeconds(waitSec))
                    .pollingEvery(ofMillis(polling))
                    .until(x -> {
                        boolean result = supplier.get();
                        return result;
                    });
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }
}

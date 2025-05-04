package guru.qa.niffler.condition;

import com.codeborne.selenide.*;
import guru.qa.niffler.model.CurrencyValues;
import guru.qa.niffler.model.rest.SpendJson;
import guru.qa.niffler.util.SupportUtils;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.String.format;

public class SpendsConditions {
    private static final int categoryNameColumnNumber = 1;
    private static final int amountColumnNumber = 2;
    private static final int descriptionColumnNumber = 3;
    private static final int dateColumnNumber = 4;

    public static WebElementsCondition spends(SpendJson... spends) {
        return new WebElementsCondition() {
            String message = "Spends mismatch with actual";
            @Override
            public String errorMessage() {
                return message;
            }

            @NotNull
            @Override
            public CheckResult check(Driver driver, List<WebElement> elements) {
                SupportUtils.checkIsNotEmpty(spends);
                if (spends.length != elements.size()) {
                    message = String.format("List size mismatch (expected: %s, actual: %s",
                            spends.length, elements.size());
                    return CheckResult.rejected(message, elements);
                }
                final List<String> cellError = new ArrayList<>();
                for (int i = 0; i < spends.length; i++) {
                    SpendJson spend = spends[i];
                    final List<String> cellsText = elements.get(i).findElements(By.xpath(".//td"))
                            .stream()
                            .map(cell -> cell.getText().trim())
                            .toList();
                    checkCell("Category",
                            cellsText.get(categoryNameColumnNumber),
                            spend.category().name(),
                            cellError);
                    checkCell("Amount",
                            getNormalizeSpendAmount(spend.amount(), spend.currency()),
                            cellsText.get(amountColumnNumber),
                            cellError);
                    checkCell("Description",
                            spend.description(),
                            cellsText.get(descriptionColumnNumber),
                            cellError);
                    checkCell("Date",
                            getDateByPattern(spend.spendDate()),
                            cellsText.get(dateColumnNumber),
                            cellError);
                    if(!cellError.isEmpty()) {
                        return CheckResult.rejected(message,
                                "\nRow %s with mismatch %s".formatted(i + 1, String.join(",", cellError)));
                    }
                }
                return CheckResult.accepted();
            }

            @Override
            public String toString() {
                return Arrays.stream(spends).map(x -> "\n" + x).collect(Collectors.joining());
            }
        };
    }

    private static String getNormalizeSpendAmount(Double amount, CurrencyValues currency) {
        if (amount > amount.intValue()) {
            String amountReplace = format("%.2f", amount).replace(",", ".");
            return "%s %s".formatted(amountReplace, currency.value);
        }
        return "%.0f %s".formatted(amount, currency.value);
    }

    private static String getDateByPattern(Date date) {
        return new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH).format(date);
    }

    private static void checkCell(String fieldName, String expected, String actual, List<String> cellError) {
        if (!actual.equals(expected)) {
            cellError.add(format("\n%s mismatch (expected: %s, actual: %s)",
                    fieldName, expected, actual));
        }
    }

}

package guru.qa.niffler.model;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@ToString
public enum DataFilter {
    ALL("All time"),
    TODAY("Today"),
    WEEK("Last week"),
    MONTH("Last month");

    public final String dateFilter;


}

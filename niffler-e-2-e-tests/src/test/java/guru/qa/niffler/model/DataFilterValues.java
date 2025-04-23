package guru.qa.niffler.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum DataFilterValues {
    ALL("All time"),
    TODAY("Today"),
    WEEK("Last week"),
    MONTH("Last month");

    public final String dateFilter;

}

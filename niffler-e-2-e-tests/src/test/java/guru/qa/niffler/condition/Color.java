package guru.qa.niffler.condition;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Color {
    yellow("rgba(255, 183, 3, 1)"),
    green("rgba(53, 173, 123, 1)");

    public final String rgb;

    public static Color fromCss(String rgba) {
        for (Color color : Color.values()) {
            if (color.rgb.equals(rgba)) {
                return color;
            }
        }
        throw new IllegalArgumentException("Цвет с rgba %s отсутствует в Color".formatted(rgba));
    }
}

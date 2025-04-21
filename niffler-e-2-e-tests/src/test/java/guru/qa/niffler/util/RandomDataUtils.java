package guru.qa.niffler.util;

import com.github.javafaker.Faker;

import javax.annotation.Nonnull;
import java.util.Locale;

public class RandomDataUtils {
    private static final Faker faker = new Faker(Locale.US);

    public static @Nonnull String randomUsername() {
        return faker.name().username() + System.currentTimeMillis();
    }
    public static @Nonnull String randomName() {
        return faker.name().name() + System.currentTimeMillis();
    }
    public static @Nonnull String randomSurname() {
        return faker.superhero().name() + System.currentTimeMillis();
    }
    public static @Nonnull String randomPassword() {
        return faker.internet().password();
    }

    public static @Nonnull String randomCategoryName() {
        return faker.book().title() + System.currentTimeMillis();
    }
    public static @Nonnull String randomSentence() {
        return faker.elderScrolls().creature() + System.currentTimeMillis();
    }

    public static @Nonnull String[] getArrayWithRandomUsername(int x) {
        String[] usernames = new String[x];
        for (int i = 0; i < x; i++) {
            usernames[i] = RandomDataUtils.randomUsername();
        }
        return usernames;
    }
}

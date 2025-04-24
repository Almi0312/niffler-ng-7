package guru.qa.niffler.util;

import com.github.javafaker.Faker;

import javax.annotation.Nonnull;
import java.util.Locale;
import java.util.Random;

public class RandomDataUtils {
    private static final Faker faker = new Faker(Locale.US);
    private static final Random random = new Random();

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

    public static @Nonnull String randomSpendName() {
        return faker.animal().name() + System.currentTimeMillis();
    }

    public static @Nonnull String[] getArrayWithRandomUsername(int x) {
        String[] usernames = new String[x];
        for (int i = 0; i < x; i++) {
            usernames[i] = RandomDataUtils.randomUsername();
        }
        return usernames;
    }

    public static @Nonnull int getRandomNumber(int start, int end) {
        return random.nextInt(start, end);
    }
}

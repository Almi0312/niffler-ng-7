package guru.qa.niffler.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class OauthUtils {
    /**
     * Это криптографически случайная строка,
     * которую клиент (ваше приложение) создаёт перед началом OAuth-потока.
     * Она нужна для защиты от атак подмены кода авторизации.
     */
    public static String generateCodeVerifier() {
        SecureRandom secureRandom = new SecureRandom(); // 1. Создаём криптографически безопасный ГСЧ
        byte[] codeVerifier = new byte[32];             // 2. Генерируем 32 случайных байта (256 бит)
        secureRandom.nextBytes(codeVerifier);           // 3. Заполняем массив случайными значениями
        return Base64.getUrlEncoder()                   // 4. Кодируем в Base64URL (без паддинга '=')
                .withoutPadding()
                .encodeToString(codeVerifier);
    }

    /**
     * это преобразованная версия code_verifier,
     * которая отправляется серверу авторизации.
     * Метод S256 означает, что code_verifier хешируется с помощью SHA-256,
     * а затем кодируется в Base64URL.
     */
    public static String generateCodeChallenge(String codeVerifier) {
        byte[] bytes = codeVerifier                              // 1. Переводим в байты криптографически безопасный ГСЧ
                .getBytes(StandardCharsets.US_ASCII);
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");  // 2. Берём SHA-256
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        md.update(bytes);                                       // 3. Хешируем verifier
        byte[] digest = md.digest();                            // 4. Получаем хеш (32 байта)
        return Base64.getUrlEncoder()                           // 5. Кодируем в Base64URL
                .withoutPadding()
                .encodeToString(digest);
    }
}
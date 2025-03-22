package guru.qa.niffler.test.web;

import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import guru.qa.niffler.model.CurrencyValues;
import guru.qa.niffler.model.UserdataUserJson;
import guru.qa.niffler.service.auth.AuthUserDBSpringChainedXaClient;
import guru.qa.niffler.service.auth.AuthUserDBSpringClient;
import guru.qa.niffler.service.userdata.UdDBSpringChainedXaClient;
import guru.qa.niffler.service.userdata.UserdataDBSpringClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static guru.qa.niffler.util.RandomDataUtils.randomName;
import static guru.qa.niffler.util.RandomDataUtils.randomSurname;

@Slf4j
public class ChainedXaTest {

    @Test
    void checkTransactionByUserTest() {
        UdDBSpringChainedXaClient userdataDBClient = new UdDBSpringChainedXaClient();
        AuthUserDBSpringChainedXaClient authUserDBClient = new AuthUserDBSpringChainedXaClient();
        String username = "chainedTransac";
        userdataDBClient.findByUsername(username)
                .ifPresent(userJson -> {
                    userdataDBClient.delete(userJson);
                    log.info("DELETED USER");
                });
        // транзакция упадет из-за явного вызова ошибки в коде в методе create
        userdataDBClient.create(
                new UserdataUserJson(
                        null,
                        username,
                        randomSurname(),
                        randomSurname(),
                        randomName(),
                        CurrencyValues.RUB,
                        null,
                        null,
                        null
                ));
        log.info("EXPECT NOT CREATED USER");
        UserdataUserJson udUser = userdataDBClient.findByUsername(username).orElse(null);
        Assertions.assertNull(udUser, "Юзер %s был создан".formatted(udUser));
        log.info("EXPECT CREATED AUTH_USER");
        AuthUserEntity authUserEntity = authUserDBClient.findUserByUsername(username).orElse(null);
        Assertions.assertNotNull(authUserEntity, "Юзер авторизации %s не был создан. Транзакция работает хорошо".formatted(authUserEntity));
        List<AuthorityEntity> authority = authUserDBClient.findAuthorityByUserId(authUserEntity);
        Assertions.assertTrue(authority.isEmpty(), "авторизация была создана %s".formatted(authority));
        Assertions.assertEquals(2, authority.size(), "авторизация создана не в полном объеме %d".formatted(authority.size()));

    }
}

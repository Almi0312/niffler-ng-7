package guru.qa.niffler.model.rest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import guru.qa.niffler.data.entity.userdata.UserdataUserEntity;
import guru.qa.niffler.model.CurrencyValues;
import guru.qa.niffler.model.FriendshipStatus;
import guru.qa.niffler.model.TestData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@ParametersAreNonnullByDefault
public record UserdataUserJson(
        @JsonProperty("id")
        UUID id,
        @JsonProperty("username")
        String username,
        @JsonProperty("firstname")
        String firstname,
        @JsonProperty("surname")
        String surname,
        @JsonProperty("fullname")
        String fullname,
        @JsonProperty("currency")
        CurrencyValues currency,
        @JsonProperty("photo")
        String photo,
        @JsonProperty("photoSmall")
        String photoSmall,
        @JsonProperty("friendshipStatus")
        FriendshipStatus friendshipStatus,
        @JsonIgnore
        TestData testData) {

    public UserdataUserJson(@Nonnull String username) {
        this(username, null);
    }

    public UserdataUserJson(@Nonnull String username, @Nullable TestData testData) {
        this(null, username, null, null, null, null, null, null, null, testData);
    }

    public static @Nonnull UserdataUserJson fromEntity(@Nonnull UserdataUserEntity entity, @Nonnull FriendshipStatus friendshipStatus) {
        return new UserdataUserJson(
                entity.getId(),
                entity.getUsername(),
                entity.getFirstname(),
                entity.getSurname(),
                entity.getFullname(),
                entity.getCurrency(),
                entity.getPhoto() != null && entity.getPhoto().length > 0 ? new String(entity.getPhoto(), StandardCharsets.UTF_8) : null,
                entity.getPhotoSmall() != null && entity.getPhotoSmall().length > 0 ? new String(entity.getPhotoSmall(), StandardCharsets.UTF_8) : null,
                friendshipStatus,
                null
        );
    }

    public @Nonnull UserdataUserJson addTestData(@Nonnull TestData testData) {
        return new UserdataUserJson(id, username, firstname, surname, fullname, currency, photo, photoSmall, friendshipStatus, testData);
    }
}

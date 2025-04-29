package guru.qa.niffler.data.entity.userdata;

import guru.qa.niffler.model.CurrencyValues;
import guru.qa.niffler.model.rest.UserdataUserJson;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

@Getter
@Setter
@Entity
@Table(name = "\"user\"")
public class UserdataUserEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, columnDefinition = "UUID default gen_random_uuid()")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CurrencyValues currency;

    @Column()
    private String firstname;

    @Column()
    private String surname;

    @Column(name = "full_name")
    private String fullname;

    @Column(name = "photo", columnDefinition = "bytea")
    private byte[] photo;

    @Column(name = "photo_small", columnDefinition = "bytea")
    private byte[] photoSmall;

    @OneToMany(mappedBy = "requester", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FriendshipEntity> friendshipRequests = new ArrayList<>();

    @OneToMany(mappedBy = "addressee", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FriendshipEntity> friendshipAddressees = new ArrayList<>();

    public static @Nonnull UserdataUserEntity fromJson(@Nonnull UserdataUserJson userJson) {
        String photo = userJson.photo();
        String photoSmall = userJson.photoSmall();
        UserdataUserEntity userdataUserEntity = new UserdataUserEntity();
        userdataUserEntity.setId(userJson.id());
        userdataUserEntity.setUsername(userJson.username());
        userdataUserEntity.setCurrency(userJson.currency());
        userdataUserEntity.setFirstname(userJson.firstname());
        userdataUserEntity.setSurname(userJson.surname());
        userdataUserEntity.setFullname(userJson.fullname());
        userdataUserEntity.setPhoto(photo != null ? photo.getBytes(StandardCharsets.UTF_8) : null);
        userdataUserEntity.setPhotoSmall(photoSmall != null ? photoSmall.getBytes(StandardCharsets.UTF_8) : null);
        return userdataUserEntity;
    }

    public void addFriends(FriendshipStatus status, UserdataUserEntity... friends) {
        List<FriendshipEntity> friendsEntities = Stream.of(friends)
                .map(f -> {
                    FriendshipEntity fe = new FriendshipEntity();
                    fe.setRequester(this);
                    fe.setAddressee(f);
                    fe.setStatus(status);
                    fe.setCreatedDate(new Date());
                    return fe;
                }).toList();
        this.friendshipRequests.addAll(friendsEntities);
    }

    public void addInvitations(UserdataUserEntity... invitations) {
        List<FriendshipEntity> invitationsEntities = Stream.of(invitations)
                .map(i -> {
                    FriendshipEntity fe = new FriendshipEntity();
                    fe.setRequester(i);
                    fe.setAddressee(this);
                    fe.setStatus(FriendshipStatus.PENDING);
                    fe.setCreatedDate(new Date());
                    return fe;
                }).toList();
        this.friendshipAddressees.addAll(invitationsEntities);
    }

    public void removeFriends(UserdataUserEntity... friends) {
        List<UUID> idsToBeRemoved = Arrays.stream(friends).map(UserdataUserEntity::getId).toList();
        for (Iterator<FriendshipEntity> i = getFriendshipRequests().iterator(); i.hasNext(); ) {
            FriendshipEntity friendsEntity = i.next();
            if (idsToBeRemoved.contains(friendsEntity.getAddressee().getId())) {
                friendsEntity.setAddressee(null);
                i.remove();
            }
        }
    }

    public void removeInvites(UserdataUserEntity... invitations) {
        List<UUID> idsToBeRemoved = Arrays.stream(invitations).map(UserdataUserEntity::getId).toList();
        for (Iterator<FriendshipEntity> i = getFriendshipAddressees().iterator(); i.hasNext(); ) {
            FriendshipEntity friendsEntity = i.next();
            if (idsToBeRemoved.contains(friendsEntity.getRequester().getId())) {
                friendsEntity.setRequester(null);
                i.remove();
            }
        }
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        UserdataUserEntity that = (UserdataUserEntity) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
package hello.community.domain.chat;

import hello.community.domain.groupBuy.GroupBuy;
import hello.community.domain.user.Users;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Waiting {
    @Id
    @GeneratedValue
    @Column(name = "waiting_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    @ManyToOne
    @JoinColumn(name = "groupbuy_id")
    private GroupBuy groupBuy;

    private LocalDateTime createdAt;

    private boolean accepted; // 3번 API 추가

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

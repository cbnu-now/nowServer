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

    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean accepted = false; // 기본값을 false로 설정

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

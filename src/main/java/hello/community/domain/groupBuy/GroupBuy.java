package hello.community.domain.groupBuy;

import hello.community.domain.user.Users;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class GroupBuy {
    @Id
    @GeneratedValue
    @Column(name = "groupbuy_id")
    private Long id;

    private String title;
    private String content;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String photo;

    private int headCount;
    private int currentCount;

    private String latitude;
    private String longitude;

    // 생성일자
    private LocalDateTime createdAt;

    private int view;
    private int likes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;



}

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
    private String category;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String photo;

    private Long headCount;
    private Long currentCount;

    private Double latitude;
    private Double longitude;
    private String address;
    private String spotName;


    // 생성일자
    private LocalDateTime createdAt;

    private Long view;
    private Long likes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;


}

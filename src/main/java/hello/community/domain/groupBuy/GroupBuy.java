package hello.community.domain.groupBuy;

import hello.community.domain.chat.Waiting;
import hello.community.domain.user.Users;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long currentCount = 0L;

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

    @OneToMany(mappedBy = "groupBuy", cascade = CascadeType.ALL)
    private List<Waiting> waiting = new ArrayList<>();

    public boolean isCompleted = false; // 모집 완료 여부를 나타내는 필드

    // 모집 완료 여부를 체크하고 설정하는 메서드
    public void checkAndSetCompleted() {
        this.isCompleted = this.headCount.equals(this.currentCount);
    }

    public void closeEarly() {
        this.isCompleted = true;
    }
}

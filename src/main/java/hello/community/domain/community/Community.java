package hello.community.domain.community;

import hello.community.domain.user.Users;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Community {
    @Id //id 필드는 기본 키
    @GeneratedValue //자동으로 값이 생성
    @Column(name = "community_id")  //컬럼 이름을 community_id로 지정
    private Long id;

    private String title;
    private String content;

    @Lob    //필드가 큰 객체 데이터를 저장할 때 사용됩니다. "Lob"은 "Large Object"의 약자
    @Column(columnDefinition = "TEXT")  //columnDefinition 속성은 필드의 데이터베이스 컬럼 유형을 명시적으로 정의할 때 사용
    private String photo;

    private String category;
    private Double latitude;
    private Double longitude;
    private String address;

    private Long view;
    private Long likes;

    private LocalDateTime createdAt;

    //연관 관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)  //여러 개의 Community 엔티티가 하나의 Users 엔티티에 매핑될 수 있습니다.
    @JoinColumn(name = "user_id")   //이를 통해 Community 엔티티가 어느 Users 엔티티에 속하는지 알 수 있다
    private Users user;

    @OneToMany(mappedBy = "community", cascade = CascadeType.ALL)   //하나의 Community 엔티티가 여러 개의 Comment 엔티티에 매핑될 수 있습니다.
    private List<Comment> comments = new ArrayList<>();


}

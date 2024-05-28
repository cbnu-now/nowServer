//댓글 기능을 구현

package hello.community.domain.community;

import hello.community.domain.user.Users;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity //이 클래스가 JPA 엔티티임 JPA 엔티티는 데이터베이스 테이블에 매핑
@Getter //Lombok 라이브러리를 이용해 이 클래스의 모든 필드에 대해 자동으로 getter와 setter 메소드를 생성
@Setter
public class Comment {

    @Id //이 필드가 테이블의 기본 키
    @GeneratedValue //기본 키 값을 자동으로 생성
    @Column(name = "comment_id")    //데이터베이스 테이블의 컬럼 이름을 지정합니다. 여기서는 comment_id로 설정
    private Long id;

    private String content; //댓글의 내용을 저장
    private LocalDateTime createdAt;    //댓글이 생성된 시간을 저장

    @ManyToOne(fetch = FetchType.LAZY)  //Comment 엔티티가 Users 엔티티와 다대일(Many-to-One) 관계
    @JoinColumn(name = "user_id")   //외래 키 컬럼 이름을 지정합니다. 여기서는 user_id로 설정
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)  //Comment 엔티티가 Community 엔티티와 다대일(Many-to-One) 관계
    @JoinColumn(name = "community_id")  //외래 키 컬럼 이름을 지정합니다. 여기서는 community_id로 설
    private Community community;

    private String img; //댓글에 포함된 이미지의 URL 또는 경로를 저장
}

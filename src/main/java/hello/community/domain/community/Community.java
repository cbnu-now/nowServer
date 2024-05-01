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
    @Id
    @GeneratedValue
    @Column(name = "community_id")
    private Long id;

    private String title;
    private String content;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String photo;

    private String category;
    private Double latitude;
    private Double longitude;
    private String address;

    private Long view;
    private Long likes;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;

    @OneToMany(mappedBy = "community", cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();


}

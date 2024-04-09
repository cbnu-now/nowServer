package hello.community.domain.user;

import hello.community.domain.community.Comment;
import hello.community.domain.community.Community;
import hello.community.domain.groupBuy.GroupBuy;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@Setter
public class Users {
    @Id
    @GeneratedValue
    @Column(name = "user_id")
    private Long id;

    private String phone;
    private String name;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String photo;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<GroupBuy> groupBuys = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Community> communities = new ArrayList<>();

}

package hello.community.domain.liked;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Liked {

    @Id
    @GeneratedValue
    @Column(name = "liked_id")
    private Long id;

    private Long userId;
    private Long groupBuyId;
    private Long communityId;
}

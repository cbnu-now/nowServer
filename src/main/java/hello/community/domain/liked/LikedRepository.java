package hello.community.domain.liked;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LikedRepository extends JpaRepository<Liked, Long>{
    List<Liked> findByUserId(Long userId);
}

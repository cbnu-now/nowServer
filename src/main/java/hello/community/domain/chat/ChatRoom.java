package hello.community.domain.chat;

import hello.community.domain.groupBuy.GroupBuy;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class ChatRoom {
    @Id
    @GeneratedValue
    @Column(name = "chatroom_id")
    private Long id;

    // `chatRoomName` 필드를 제거하고 `GroupBuy`와 연관 관계 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "groupbuy_id")
    private GroupBuy groupBuy;

    // 채팅
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
    private List<Chat> chats = new ArrayList<>();

    @OneToMany(mappedBy = "chatRoom")
    private List<UserChatRoom> userChatRooms = new ArrayList<>();
}

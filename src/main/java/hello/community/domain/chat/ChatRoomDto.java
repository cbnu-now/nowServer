package hello.community.domain.chat;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class ChatRoomDto {
    private Long chatRoomId;
    private List<String> userProfileImgUrls; // 채팅자들 이미지 url
    private String groupBuyThumbnailUrl; // 모집글 게시글 섬네일 이미지 url
    private String chatRoomTitle; // 채팅방 제목
    private int numberOfParticipants; // 채팅자 수
    private String lastMessage; // 마지막 채팅 메시지
    private LocalDateTime lastMessageTime; // 마지막 채팅 시간
    private int unreadMessagesCount; // 사용자가 채팅방에서 읽지 않은 메시지 개수
}

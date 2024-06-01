package hello.community.domain.chat;

import hello.community.domain.community.CommunityDto;
import hello.community.domain.user.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;

@Tag(name = "Chat", description = "채팅 관련 API 입니다.")
@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/raisehand/{groupBuyId}")
    @Operation(
            summary = "모집글에 손들기",
            description = "모집글에 손을 들어 참여를 신청합니다."
    )
    public ResponseEntity<UserDto.CheckResult> raiseHand(@PathVariable Long groupBuyId) {
        chatService.raiseHand(groupBuyId);
        return ResponseEntity.ok(UserDto.CheckResult.builder().result("손들기 완료").build());
    }

    @GetMapping("/notifications/{groupBuyId}")
    @Operation(
            summary = "손들기 알림 목록 조회",
            description = "손들기 요청한 사용자의 정보를 배열로 조회합니다."
    )
    public ResponseEntity<List<WaitingNotificationDto>> getWaitingNotifications(@PathVariable Long groupBuyId) {
        List<WaitingNotificationDto> notifications = chatService.getWaitingNotifications(groupBuyId);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/acceptWaiting/{waitingId}")
    @Operation(
            summary = "손들기 수락",
            description = "대기자의 손들기 요청을 수락합니다."
    )
    public ResponseEntity<UserDto.CheckResult> acceptWaiting(@PathVariable Long waitingId) {
        chatService.acceptWaiting(waitingId);
        return ResponseEntity.ok(UserDto.CheckResult.builder().result("수락 완료").build());
    }

    @GetMapping("/chatrooms/{userId}")
    @Operation(
            summary = "채팅방 목록 조회",
            description = "사용자가 참여하고 있는 채팅방 목록을 조회합니다."
    )
    public ResponseEntity<List<ChatRoomListDto>> getChatRoomList(@PathVariable Long userId) {
        List<ChatRoomListDto> chatRooms = chatService.getChatRoomList(userId);
        return ResponseEntity.ok(chatRooms);
    }
}

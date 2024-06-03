package hello.community.domain.chat;

import hello.community.domain.user.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<UserDto.CheckResult> raiseHand(@PathVariable Long groupBuyId, @RequestHeader("Authorization") String token) {
        chatService.raiseHand(groupBuyId, token);
        return ResponseEntity.ok(UserDto.CheckResult.builder().result("손들기 완료").build());
    }

    @GetMapping("/notifications")
    @Operation(
            summary = "손들기 알림 목록 조회",
            description = "손들기 요청한 사용자의 정보를 배열로 조회합니다."
    )
    public ResponseEntity<List<WaitingNotificationDto>> getWaitingNotifications(@RequestHeader("Authorization") String token) {
        List<WaitingNotificationDto> notifications = chatService.getWaitingNotifications(token);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/acceptWaiting/{waitingId}")
    @Operation(
            summary = "손들기 수락",
            description = "대기자의 손들기 요청을 수락합니다."
    )
    public ResponseEntity<UserDto.CheckResult> acceptWaiting(@PathVariable Long waitingId, @RequestHeader("Authorization") String token) {
        chatService.acceptWaiting(waitingId, token);
        return ResponseEntity.ok(UserDto.CheckResult.builder().result("수락 완료").build());
    }

    @GetMapping("/chatrooms")
    @Operation(
            summary = "채팅방 목록 조회",
            description = "사용자가 참여하고 있는 채팅방 목록을 조회합니다."
    )
    public ResponseEntity<List<ChatRoomListDto>> getChatRoomList(@RequestHeader("Authorization") String token) {
        List<ChatRoomListDto> chatRooms = chatService.getChatRoomList(token);
        return ResponseEntity.ok(chatRooms);
    }

    @PutMapping("/chatrooms/{chatRoomId}/messages")
    @Operation(
            summary = "채팅 메시지 전송",
            description = "채팅방에 메시지를 전송합니다."
    )
    public ResponseEntity<UserDto.CheckResult> sendMessage(@PathVariable Long chatRoomId, @RequestBody ChatMessageDto chatMessageDto, @RequestHeader("Authorization") String token) {
        chatService.sendMessage(chatRoomId, chatMessageDto, token);
        return ResponseEntity.ok(UserDto.CheckResult.builder().result("메시지 전송 완료").build());
    }

    @GetMapping("/chatrooms/{chatRoomId}/records")
    @Operation(
            summary = "채팅방 상세 기록 조회",
            description = "채팅방의 상세 채팅 기록을 조회합니다."
    )
    public ResponseEntity<List<ChatRecordDto>> getChatRecords(@PathVariable Long chatRoomId, @RequestHeader("Authorization") String token) {
        List<ChatRecordDto> chatRecords = chatService.getChatRecords(chatRoomId, token);
        return ResponseEntity.ok(chatRecords);
    }

    @DeleteMapping("/chatrooms/{chatRoomId}/leave")
    @Operation(
            summary = "채팅방 나가기",
            description = "사용자가 특정 채팅방에서 나갑니다."
    )
    public ResponseEntity<UserDto.CheckResult> leaveChatRoom(@PathVariable Long chatRoomId, @RequestHeader("Authorization") String token) {
        chatService.leaveChatRoom(chatRoomId, token);
        return ResponseEntity.ok(UserDto.CheckResult.builder().result("채팅방 나가기 완료").build());
    }

}

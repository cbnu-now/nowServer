package hello.community.domain.chat;

import hello.community.domain.community.CommunityDto;
import hello.community.domain.user.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/waitings/{groupBuyId}")
    @Operation(
            summary = "대기자 목록 조회",
            description = "특정 모집글에 대한 대기자 목록을 조회합니다."
    )

    public ResponseEntity<List<WaitingDto>> getWaitingsForGroupBuy(@PathVariable Long groupBuyId) {
        List<WaitingDto> waitings = chatService.getWaitingsForGroupBuy(groupBuyId);
        return ResponseEntity.ok(waitings);
    }

    @PutMapping("/acceptWaiting/{waitingId}")
    @Operation(
            summary = "손들기 수락",
            description = "대기자를 수락하여 채팅방을 생성합니다."
    )

    public ResponseEntity<UserDto.CheckResult> acceptWaiting(@PathVariable Long waitingId) {
        chatService.acceptWaiting(waitingId);
        return ResponseEntity.ok(UserDto.CheckResult.builder().result("수락 완료").build());
    }

    @GetMapping("/chatrooms")
    @Operation(
            summary = "채팅방 목록 조회",
            description = "모든 채팅방 목록을 조회합니다."
    )
    public ResponseEntity<List<ChatRoomDto>> getAllChatRooms() {
        List<ChatRoomDto> chatRooms = chatService.getAllChatRooms();
        return ResponseEntity.ok(chatRooms);
    }

    @GetMapping("/chatrooms/{chatRoomId}/messages")
    @Operation(
            summary = "채팅방 메시지 조회",
            description = "특정 채팅방의 모든 메시지를 조회합니다."
    )
    public ResponseEntity<List<ChatDto>> getChatMessages(@PathVariable Long chatRoomId) {
        List<ChatDto> chatMessages = chatService.getChatMessages(chatRoomId);
        return ResponseEntity.ok(chatMessages);
    }

    @PostMapping("/chatrooms/{chatRoomId}/messages")
    @Operation(
            summary = "채팅 메시지 전송",
            description = "특정 채팅방에 메시지를 전송합니다."
    )
    public ResponseEntity<UserDto.CheckResult> sendMessage(@PathVariable Long chatRoomId, @RequestBody ChatDto chatDto) {
        chatService.sendMessage(chatRoomId, chatDto.getContent());
        return ResponseEntity.ok(UserDto.CheckResult.builder().result("메시지 전송 완료").build());
    }

    @DeleteMapping("/chatrooms/{chatRoomId}/leave")
    @Operation(
            summary = "채팅방 나가기",
            description = "특정 채팅방에서 나갑니다."
    )
    public ResponseEntity<UserDto.CheckResult> leaveChatRoom(@PathVariable Long chatRoomId) {
        chatService.leaveChatRoom(chatRoomId);
        return ResponseEntity.ok(UserDto.CheckResult.builder().result("채팅방 나가기 완료").build());
    }
}
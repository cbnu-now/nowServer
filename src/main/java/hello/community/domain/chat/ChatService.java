package hello.community.domain.chat;

import hello.community.domain.groupBuy.GroupBuy;
import hello.community.domain.groupBuy.GroupBuyRepository;
import hello.community.domain.user.UserRepository;
import hello.community.domain.user.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatService {

    private final WaitingRepository waitingRepository;
    private final GroupBuyRepository groupBuyRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserChatRoomRepository userChatRoomRepository;

    public void raiseHand(Long groupBuyId) {
        GroupBuy findedGroupBuy = groupBuyRepository.findById(groupBuyId).orElseThrow(() -> new IllegalArgumentException("해당 모집글이 존재하지 않습니다."));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Long userId = Long.parseLong(username);
        Users user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (findedGroupBuy.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("본인이 작성한 모집글에는 참여할 수 없습니다.");
        }

        Waiting waiting = new Waiting();
        waiting.setUser(user);
        waiting.setGroupBuy(findedGroupBuy);
        waitingRepository.save(waiting);
    }
    //대기자 정보를 담기 위한 DTO
    public List<WaitingDto> getWaitingsForGroupBuy(Long groupBuyId) {
        return waitingRepository.findByGroupBuyId(groupBuyId)
                .stream()
                .map(waiting -> new WaitingDto(
                        waiting.getId(),
                        waiting.getUser().getName(),
                        waiting.getUser().getPhoto(),
                        waiting.getGroupBuy().getId(),
                        waiting.getGroupBuy().getTitle()
                ))
                .collect(Collectors.toList());
    }
    //대기자 상태를 업데이트하고 필요 시 채팅방을 생성하는 로직
    public void acceptWaiting(Long waitingId) {
        Waiting waiting = waitingRepository.findById(waitingId).orElseThrow(() -> new IllegalArgumentException("해당 대기자가 존재하지 않습니다."));
        GroupBuy groupBuy = waiting.getGroupBuy();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Long userId = Long.parseLong(username);
        Users user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!groupBuy.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("모집글 작성자만 대기자를 수락할 수 있습니다.");
        }

        waiting.setAccepted(true);
        waitingRepository.save(waiting);

        List<Waiting> acceptedWaitings = waitingRepository.findByGroupBuyIdAndAccepted(groupBuy.getId(), true);
        if (acceptedWaitings.size() >= groupBuy.getMaxParticipants()) {
            ChatRoom chatRoom = new ChatRoom();
            chatRoom.setChatRoomName(groupBuy.getTitle());
            chatRoomRepository.save(chatRoom);

            for (Waiting acceptedWaiting : acceptedWaitings) {
                UserChatRoom userChatRoom = new UserChatRoom();
                userChatRoom.setUser(acceptedWaiting.getUser());
                userChatRoom.setChatRoom(chatRoom);
                userChatRoomRepository.save(userChatRoom);
            }
        }
    }
    //모든 채팅방을 조회하여 반환하는 메서드를 추가
    public List<ChatRoomDto> getAllChatRooms() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Long userId = Long.parseLong(username);
        userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<ChatRoom> chatRooms = chatRoomRepository.findAll();
        return chatRooms.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    private ChatRoomDto convertToDto(ChatRoom chatRoom) {
        return ChatRoomDto.builder()
                .id(chatRoom.getId())
                .chatRoomName(chatRoom.getChatRoomName())
                .build();
    }

    //특정 채팅방의 채팅 기록을 조회하여 반환하는 메서드를 추가
    public List<ChatDto> getChatMessages(Long chatRoomId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Long userId = Long.parseLong(username);
        userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<Chat> chats = chatRepository.findByChatRoomId(chatRoomId);
        return chats.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    private ChatDto convertToDto(Chat chat) {
        return ChatDto.builder()
                .id(chat.getId())
                .content(chat.getContent())
                .createdAt(chat.getCreatedAt())
                .username(chat.getUser().getName())
                .userProfileImageUrl(chat.getUser().getPhoto())
                .isMyMessage(chat.getUser().getId().equals(Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName())))
                .build();
    }
    //채팅 메시지를 전송할 때 해당 메시지
    public void sendMessage(Long chatRoomId, String content) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Long userId = Long.parseLong(username);
        Users user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(() -> new IllegalArgumentException("해당 채팅방이 존재하지 않습니다."));

        Chat chat = new Chat();
        chat.setContent(content);
        chat.setCreatedAt(LocalDateTime.now());
        chat.setUser(user);
        chat.setChatRoom(chatRoom);

        chatRepository.save(chat);
    }

    // 유저를 채팅방에서 제거하는 메서드를 추가
    public void leaveChatRoom(Long chatRoomId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Long userId = Long.parseLong(username);
        Users user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(() -> new IllegalArgumentException("해당 채팅방이 존재하지 않습니다."));

        UserChatRoom userChatRoom = userChatRoomRepository.findByUserIdAndChatRoomId(userId, chatRoomId);
        if (userChatRoom != null) {
            userChatRoomRepository.delete(userChatRoom);
        } else {
            throw new IllegalArgumentException("채팅방에 존재하지 않는 사용자입니다.");
        }
    }
}
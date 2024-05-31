package hello.community.domain.user;

import hello.community.domain.community.Community;
import hello.community.domain.community.CommunityDto;
import hello.community.domain.community.CommunityRepository;
import hello.community.domain.groupBuy.GroupBuy;
import hello.community.domain.groupBuy.GroupBuyDto;
import hello.community.domain.groupBuy.GroupBuyRepository;
import hello.community.domain.liked.Liked;
import hello.community.domain.liked.LikedRepository;
import hello.community.global.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final GroupBuyRepository groupBuyRepository;
    private final CommunityRepository communityRepository;
    private final LikedRepository likedRepository;

    //회원가입
    public Long join(UserDto.JoinInfo joinDto) {
        Users user = new Users();
        user.setPhone(joinDto.getPhone());
        user.setName(joinDto.getName());
        userRepository.save(user);
        Long userId = userRepository.findByPhone(joinDto.getPhone()).getId();
        return userId;
    }

    // 전화번호 중복 확인
    public Long isExistPhone(UserDto.PhoneNumCheck phoneNumCheck) {
        Users user = userRepository.findByPhone(phoneNumCheck.getPhone());
        if (user == null) {
            return null;
        }
        return user.getId();
    }

    public void deleteUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Long userId = Long.parseLong(username);
        userRepository.deleteById(userId);
    }

    public String signIn(String phone) {
        Users user = userRepository.findByPhone(phone);
        String token;
        if (user == null) {
            throw new IllegalArgumentException("해당 전화번호의 사용자를 찾을 수 없습니다.");
        }

        token = jwtService.createToken(user.getId());    // 토큰 생성
        return token;    // 생성자에 토큰 추가
    }



    public UserDto.ProvideInfo getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Long userId = Long.parseLong(username);
        Users user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        UserDto.ProvideInfo provideInfo = UserDto.ProvideInfo.builder()
                .name(user.getName())
                .photo(user.getPhoto())
                .id(user.getId())
                .phone(user.getPhone())
                .build();
        return provideInfo;
    }


    public boolean nameDobuleCheck(String name) {
        Users findedName = userRepository.findByName(name);
        return findedName != null;
    }

    public void updateUserInfo(String url,String name) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Long userId = Long.parseLong(username);
        Users user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if(url!=null)
            user.setPhoto(url);
        if(name!=null)
            user.setName(name);
    }

    public List<GroupBuyDto.viewGroupBuyListInfo> getMyGroupBuyList() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Long userId = Long.parseLong(username);
        List<GroupBuy> byUserId = groupBuyRepository.findByUserId(userId);
        List<GroupBuyDto.viewGroupBuyListInfo> GroupBuyList = new ArrayList<>();

        List<Liked> likedList = likedRepository.findByUserId(userId);
        Set<Long> likedGroupBuyIds = likedList.stream().map(Liked::getGroupBuyId).collect(Collectors.toSet());

        for (GroupBuy groupBuy : byUserId) {
            boolean isLiked = likedGroupBuyIds.contains(groupBuy.getId());

            GroupBuyDto.viewGroupBuyListInfo viewGroupBuyListInfo = GroupBuyDto.viewGroupBuyListInfo.builder()
                        .title(groupBuy.getTitle())
                        .img(groupBuy.getPhoto())
                        .Category(groupBuy.getCategory())
                        .createdAt(groupBuy.getCreatedAt())
                        .headCount(groupBuy.getHeadCount())
                        .currentCount(groupBuy.getCurrentCount())
                        .likes(groupBuy.getLikes())
                        .id(groupBuy.getId())
                        .isLiked(isLiked)
                        .build();
                GroupBuyList.add(viewGroupBuyListInfo);
        }

        return GroupBuyList;
    }

    public List<CommunityDto.viewCommunityListInfo> getMyCommunity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Long userId = Long.parseLong(username);
        List<Community> byUserId = communityRepository.findByUserId(userId);
        List<CommunityDto.viewCommunityListInfo> GroupBuyList = new ArrayList<>();

        for (Community community : byUserId) {

            CommunityDto.viewCommunityListInfo viewGroupBuyListInfo = CommunityDto.viewCommunityListInfo.builder()
                    .title(community.getTitle())
                    .img(community.getPhoto())
                    .category(community.getCategory())
                    .createdAt(community.getCreatedAt())
                    .likes(community.getLikes())
                    .id(community.getId())
                    .content(community.getContent())
                    .address(community.getAddress())
                    .build();

            GroupBuyList.add(viewGroupBuyListInfo);
        }

        return GroupBuyList;
    }

    public List<GroupBuyDto.viewGroupBuyListInfo> getMyGroupBuyLikeList() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Long userId = Long.parseLong(username);
        List<Liked> likedList = likedRepository.findByUserId(userId);
        List<GroupBuyDto.viewGroupBuyListInfo> GroupBuyList = new ArrayList<>();


        for (Liked liked : likedList) {
            if(liked.getGroupBuyId() == null)
                continue;
            GroupBuy groupBuy = groupBuyRepository.findById(liked.getGroupBuyId()).orElseThrow(() -> new IllegalArgumentException("해당 모집글이 존재하지 않습니다."));
            boolean isLiked = true;

            GroupBuyDto.viewGroupBuyListInfo viewGroupBuyListInfo = GroupBuyDto.viewGroupBuyListInfo.builder()
                    .title(groupBuy.getTitle())
                    .img(groupBuy.getPhoto())
                    .Category(groupBuy.getCategory())
                    .createdAt(groupBuy.getCreatedAt())
                    .headCount(groupBuy.getHeadCount())
                    .currentCount(groupBuy.getCurrentCount())
                    .likes(groupBuy.getLikes())
                    .id(groupBuy.getId())
                    .isLiked(isLiked)
                    .build();
            GroupBuyList.add(viewGroupBuyListInfo);
        }
        return GroupBuyList;

    }

    public List<CommunityDto.viewCommunityListInfo> getMyCommunityLike() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Long userId = Long.parseLong(username);
        List<Liked> likedList = likedRepository.findByUserId(userId);
        List<CommunityDto.viewCommunityListInfo> communityList = new ArrayList<>();

        for (Liked liked : likedList) {
            if(liked.getCommunityId() == null)
                continue;
            Community community = communityRepository.findById(liked.getCommunityId()).orElseThrow(() -> new IllegalArgumentException("해당 커뮤니티 글이 존재하지 않습니다."));
            CommunityDto.viewCommunityListInfo viewCommunityListInfo = CommunityDto.viewCommunityListInfo.builder()
                    .title(community.getTitle())
                    .img(community.getPhoto())
                    .category(community.getCategory())
                    .createdAt(community.getCreatedAt())
                    .likes(community.getLikes())
                    .id(community.getId())
                    .content(community.getContent())
                    .address(community.getAddress())
                    .build();
            communityList.add(viewCommunityListInfo);
        }
        return communityList;

    }
}

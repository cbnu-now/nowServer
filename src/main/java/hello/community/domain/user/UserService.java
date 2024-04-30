package hello.community.domain.user;

import hello.community.global.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final JwtService jwtService;

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



    public Users getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Long userId = Long.parseLong(username);
        return userRepository.findById(userId).get();
    }


    public boolean nameDobuleCheck(String name) {
        Users findedName = userRepository.findByName(name);
        return findedName != null;
    }
}

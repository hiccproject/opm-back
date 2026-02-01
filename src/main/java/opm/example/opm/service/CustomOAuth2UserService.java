package opm.example.opm.service;

import opm.example.opm.domain.member.Member;
import opm.example.opm.domain.member.Role;
import opm.example.opm.repository.MemberRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final MemberRepository memberRepository;
    private final HttpSession httpSession;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 구글에서 유저 정보 가져오기
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 2. 현재 로그인 진행 중인 서비스 구분 (구글, 네이버 등)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 3. OAuth2 로그인 진행 시 키가 되는 필드값 (PK) (구글은 "sub")
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        // 4. 구글에서 가져온 유저 속성들 (이메일, 이름 등)
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 5. 회원가입/업데이트 로직 수행 (아래 extractAttributes 메서드 사용)
        Member member = saveOrUpdate(attributes);

        // 6. 세션에 사용자 정보 저장 (나중에 화면에서 쓰기 위함) -> 일단은 생략 가능하나 원칙상 DTO를 써야 함.
        // 여기서는 간단하게 로직만 구현

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(member.getRole().getKey())),
                attributes,
                userNameAttributeName);
    }

    // 유저 정보를 DB에 저장하거나 업데이트하는 메서드
    private Member saveOrUpdate(Map<String, Object> attributes) {
        // 구글은 attributes 안에 "name", "email", "picture" 키값으로 데이터를 줍니다.
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        // String picture = (String) attributes.get("picture");

        Member member = memberRepository.findByEmail(email)
                .map(entity -> entity.update(name)) // 이미 있으면 정보 업데이트
                .orElse(Member.builder() // 없으면 새로 생성 (회원가입)
                        .name(name)
                        .email(email)
                        .role(Role.GUEST) // ★ 중요: 처음엔 손님(GUEST)으로 설정
                        .build());

        return memberRepository.save(member);
    }
}
package opm.example.opm.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import opm.example.opm.domain.member.Member;
import opm.example.opm.dto.memberResponse.MemberResponseDto;
import opm.example.opm.repository.MemberRepository;
import opm.example.opm.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.core.userdetails.UserDetails; // ★ 변경된 import
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
public class MyPageController {

    private final MemberRepository memberRepository;
    private final MemberService memberService;

    // [조회] 마이페이지 정보 (JSON 반환)
    @GetMapping("/api/mypage")
    public ResponseEntity<?> getMyPageInfo(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        String email = userDetails.getUsername();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보가 없습니다."));

        // DTO로 변환해서 반환 (순환 참조 방지 및 깔끔한 데이터)
        return ResponseEntity.ok(MemberResponseDto.from(member));
    }

    // [수정] 정보 수정
    @PostMapping("/api/mypage/update")
    public ResponseEntity<?> updateMemberInfo(@AuthenticationPrincipal UserDetails userDetails,
                                              @RequestParam("name") String name) {
        String email = userDetails.getUsername();
        memberService.updateMember(email, name);

        Map<String, String> response = new HashMap<>();
        response.put("message", "정보가 수정되었습니다.");
        response.put("redirectUrl", "/mypage"); // 프론트가 새로고침하거나 이동

        return ResponseEntity.ok(response);
    }

    // [수정] 프로필 사진 변경
    @PostMapping(value="/api/mypage/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfileImage(@AuthenticationPrincipal UserDetails userDetails,
                                                @RequestParam("file") MultipartFile file) throws IOException {
        String email = userDetails.getUsername();
        if (!file.isEmpty()) {
            memberService.updateProfilePicture(email, file);
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "프로필 사진이 변경되었습니다.");
        response.put("redirectUrl", "/mypage");

        return ResponseEntity.ok(response);
    }

    // [삭제] 회원 탈퇴
    @DeleteMapping("/api/mypage/delete") // RESTful하게 DeleteMapping 사용 권장
    public ResponseEntity<?> deleteAccount(@AuthenticationPrincipal UserDetails userDetails,
                                           HttpServletRequest request) {
        String email = userDetails.getUsername();
        memberService.deleteMember(email);

        // 세션 로그아웃 처리
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();

        Map<String, String> response = new HashMap<>();
        response.put("message", "탈퇴 완료");
        response.put("redirectUrl", "/");

        return ResponseEntity.ok(response);
    }
}


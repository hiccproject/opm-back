package HICC_Project.OnePageMe.domain;


import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor // 빈 생성자 자동 생성
@Entity // DB 테이블과 매칭됨을 알림
public class Member {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column // 비밀번호 필드 추가 (OAuth 회원은 처음엔 null일 수 있음)
    private String password;

    @Enumerated(EnumType.STRING) // DB에 숫자가 아닌 문자열(USER, ADMIN)로 저장
    @Column(nullable = false)
    private Role role;

    @Builder // 빌더 패턴으로 객체 생성 쉽게 만들기
    public Member(String name, String email, Role role) {
        this.name = name;
        this.email = email;
        this.role = role;
    }

    // 이미 가입된 회원이 정보(이름, 사진)를 수정했을 때 업데이트하는 메서드
    public Member update(String name) {
        this.name = name;
        return this;
    }

    // 추가 정보 입력을 처리할 비즈니스 로직 메서드 추가
    public void completeSignup(String name, String password) {
        this.name = name;      // 이름 수정 가능하게
        this.password = password; // 비밀번호 저장
        this.role = Role.USER;    // 등급을 GUEST -> USER로 승격!
    }
}

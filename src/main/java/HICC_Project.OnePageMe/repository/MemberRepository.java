package HICC_Project.OnePageMe.repository;

import HICC_Project.OnePageMe.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// JpaRepository<Entity클래스, PK타입>을 상속받으면 기본적인 CRUD가 자동 생성됩니다.
public interface MemberRepository extends JpaRepository<Member, Long> {

    // 이메일로 이미 가입된 회원인지 확인하는 메서드
    Optional<Member> findByEmail(String email);
}

package opm.example.opm.repository;

import opm.example.opm.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    // 이메일로 회원을 찾는 기능 (중복 가입 방지용)
    Optional<Member> findByEmail(String email);
}

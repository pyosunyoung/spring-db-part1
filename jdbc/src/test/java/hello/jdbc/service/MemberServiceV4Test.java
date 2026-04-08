package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepository;
import hello.jdbc.repository.MemberRepositoryV3;
import hello.jdbc.repository.MemberRepositoryV4_1;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * 예외 누수 문제 해결
 * SQLException 제거
 *
 * MemberRepository 인터페이스에 의존
 */
@Slf4j
@SpringBootTest
class MemberServiceV4Test {

    public static final String MEMBER_A = "memberA"; // 상수?
    public static final String MEMBER_B = "memberB";
    public static final String MEMBER_EX = "ex";

    @Autowired // 의존 관계 주입 => 스프링 빈 등록 필요
    private MemberRepository memberRepository;
    @Autowired
    private MemberServiceV4 memberService;// 이 의존 관계 주입은 프록시를 주입받은 것.

    @TestConfiguration // 스프링 빈 등록 완료. => 프록시에서 해당 부분을 가져다가 씀.
    static class TestConfig {

        private final DataSource dataSource;

        public TestConfig(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        @Bean
        MemberRepository memberRepository() throws SQLException {
            return new MemberRepositoryV4_1(dataSource);
        }

        @Bean
        MemberServiceV4 memberServiceV4() throws SQLException {
            return new MemberServiceV4(memberRepository());
        }
    }

    @AfterEach
    void after() { // 테스트 실행 후 데이터를 지워 줌.
        memberRepository.delete(MEMBER_A);
        memberRepository.delete(MEMBER_B);
        memberRepository.delete(MEMBER_EX);

    }

    @Test
    void AopCheck() {
        log.info("memberService class={}", memberService.getClass());
        log.info("memberRepository class={}", memberRepository.getClass());
        Assertions.assertThat(AopUtils.isAopProxy(memberService)).isTrue();

        Assertions.assertThat(AopUtils.isAopProxy(memberRepository)).isFalse();
    }

//    memberService class=class hello.jdbc.service.MemberServiceV3_3$
//    $EnhancerBySpringCGLIB$$...
//    memberRepository class=class hello.jdbc.repository.MemberRepositoryV3
//```
//    먼저 AOP 프록시가 적용되었는지 확인해보자. AopCheck() 의 실행 결과를 보면 memberService 에
//    EnhancerBySpringCGLIB.. 라는 부분을 통해 프록시(CGLIB)가 적용된 것을 확인할 수 있다.
//    memberRepository 에는 AOP를 적용하지 않았기 때문에 프록시가 적용되지 않는다.
//    나머지 테스트 코드들을 실행해보면 트랜잭션이 정상 수행되고, 실패시 정상 롤백된 것을 확인할 수 있다
    @Test
    @DisplayName("정상 이체")
    void accountTransfer() {
        //given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberB = new Member(MEMBER_B, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberB);

        //when
        log.info("START TX");
        memberService.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);
        log.info("END TX");

        //then
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberB.getMemberId());

        Assertions.assertThat(findMemberA.getMoney()).isEqualTo(8000);
        Assertions.assertThat(findMemberB.getMoney()).isEqualTo(12000);
    }

    @Test
    @DisplayName("이체중 예외 발생")
    void accountTransferEx() {
        //given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberEx = new Member(MEMBER_EX, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberEx);

        //when
        Assertions.assertThatThrownBy(() -> memberService.accountTransfer(memberA.getMemberId(), memberEx.getMemberId(), 2000))
                .isInstanceOf(IllegalStateException.class);

        //then
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberEx.getMemberId());

        Assertions.assertThat(findMemberA.getMoney()).isEqualTo(10000); // 8000 => 10000으로 변경 롤백되었기 떄문.
        Assertions.assertThat(findMemberB.getMoney()).isEqualTo(10000);
    }

}


package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.SQLException;

/**
 * 트랜잭션 - @Transactional AOP
 */
@Slf4j
public class MemberServiceV3_3 {

    private final MemberRepositoryV3 memberRepository;

    public MemberServiceV3_3(MemberRepositoryV3 memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional //  이 어노테이션은 이 메서드가 호출되어질 떄 트랜잭션을 실행하겠다는 의미 : AOP 프록시를 적용한 트랜잭션, 클래스에도 적용 가능.
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        //트랜잭션 템플릿 호츨 트랜잭션 시작, 근데 트랜잭션 처리 기술 로직이 포함 => 서비스 로직은 비지니스 로직만 있어야하는데 이건  => aop로 프록시 도입으로 해결
            //비지니스로직 시작.
                bizLogic(fromId, toId, money);




    }

    private void bizLogic( String fromId, String toId, int money) throws SQLException {
        //트랜잭션 시작, 비지니스 로직
        Member fromMember = memberRepository.findById( fromId);
        Member toMember = memberRepository.findById(toId);

        memberRepository.update(fromId, fromMember.getMoney() - money);
        validation(toMember);
        memberRepository.update(toId, toMember.getMoney() + money);
    }


    private static void validation(Member toMember) {
        if(toMember.getMemberId().equals("ex")){
            throw new IllegalStateException("이체중 예외 발생");
        }
    }
}

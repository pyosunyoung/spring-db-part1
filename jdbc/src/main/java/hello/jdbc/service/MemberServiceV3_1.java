package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜잭션 - 트랜잭션 매니저
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV3_1 {
    //datasource를 직접 사용하는 것이 문제 jdbc를 그대로 사용하면 추후에 예로 jpa로 변경했는데 서비스 코드까지 변경되어지게됨
    // 즉 datasource에 의존 하지 않게 공동으로 사용할 수 있게 변경해보자.
    private final PlatformTransactionManager transactionManager;
    private final MemberRepositoryV3 memberRepository;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        //트랜잭션 시작
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());// 이게 기본값으로 넣어줘야함.

        try {

            //비지니스 로직 수행.

            bizLogic(fromId, toId, money);

            transactionManager.commit(status); // 성공 시 커밋
            //커밋, 롤백
        } catch (Exception e) {
           transactionManager.rollback(status); // 실패 시 롤백
            throw new IllegalStateException(e);
        } //release도 필요가 없어짐, 트랜잭션이 커밋되거나 롤백되어지면 알아서 트랜잭션이 종료되어서 알아서 닫아줌.



    }

    private void bizLogic( String fromId, String toId, int money) throws SQLException {
        //트랜잭션 시작, 비지니스 로직
        Member fromMember = memberRepository.findById( fromId);
        Member toMember = memberRepository.findById(toId);

        memberRepository.update(fromId, fromMember.getMoney() - money);
        validation(toMember);
        memberRepository.update(toId, toMember.getMoney() + money);
    }

//    private static void release(Connection con) {
//        if (con != null) {
//            try{
//                con.setAutoCommit(true); //커넥션 풀일 시 고려, 그냥 close 시 커넥션 풀에선 계속 false로 해당 커밋이 돌아갈 수 있어서 기본값 true로 따로 설정해줌.
//                con.close();
//            } catch (Exception e) {
//                log.info("error", e);
//            }
//        }
////        finally {..} 를 사용해서 커넥션을 모두 사용하고 나면 안전하게 종료한다. 그런데 커넥션 풀을 사용
////        하면 con.close() 를 호출 했을 때 커넥션이 종료되는 것이 아니라 풀에 반납된다. 현재 수동 커밋 모드
////        로 동작하기 때문에 풀에 돌려주기 전에 기본 값인 자동 커밋 모드로 변경하는 것이 안전하다.
//    }

    private static void validation(Member toMember) {
        if(toMember.getMemberId().equals("ex")){
            throw new IllegalStateException("이체중 예외 발생");
        }
    }
}

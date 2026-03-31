package hello.repository;

import hello.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import java.sql.SQLException;


@Slf4j
class MemberRepositoryV0Test {

    MemberRepositoryV0 repository = new MemberRepositoryV0();

    @Test
    void curd() throws SQLException {
        //save
        Member member = new Member("memberV1", 10000);
        repository.save(member);

        //findById
        Member findMember = repository.findById(member.getMemberId());
        log.info("findMember={}", findMember);
        assertThat(findMember).isEqualTo(member);
    } // 위 member와 findbyid로 찾은 member는 다름 findById에서는 sql로 반환된 값을 토대로 새로 객체를 만들어줬으니 다름.
}

//참고로 실행 결과에 member 객체의 참조 값이 아니라 실제 데이터가 보이는 이유는 롬복의 @Data 가
//toString() 을 적절히 오버라이딩 해서 보여주기 때문이다.
//isEqualTo() : findMember.equals(member) 를 비교한다. 결과가 참인 이유는 롬복의 @Data 는 해당
//객체의 모든 필드를 사용하도록 equals() 를 오버라이딩 하기 때문이다. ??
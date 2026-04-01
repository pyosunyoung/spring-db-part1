package hello.repository;

import com.zaxxer.hikari.HikariDataSource;
import hello.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static hello.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@Slf4j
class MemberRepositoryV1Test {
    MemberRepositoryV1 repository;

    @BeforeEach // 테스트가 실행되기 직전에 실행됨
    void beforeEach() throws SQLException {
        //기본 DriverManager - 항상 새로운 커넥션을 획득
//        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
//
//        repository = new MemberRepositoryV1(dataSource);

        //커넥션 풀링
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);

        repository = new MemberRepositoryV1(dataSource);
    }



    @Test
    void curd() throws SQLException {
        //save
        Member member = new Member("memberV110", 10000);
        repository.save(member);

        //findById
        Member findMember = repository.findById(member.getMemberId());
        log.info("findMember={}", findMember);
        assertThat(findMember).isEqualTo(member);

        //update: money : 10000 > 20000
        repository.update(member.getMemberId(), 20000);
        Member updateMember = repository.findById(member.getMemberId());
        assertThat(updateMember.getMoney()).isEqualTo(20000); //update한 값이 20000만원이 맞는지 해당 updatemember의 id를 들고와서 비교

        //delete
        repository.delete(member.getMemberId());
        assertThatThrownBy(() -> repository.findById(member.getMemberId())).isInstanceOf(NoSuchElementException.class);
        //member에 해당 member가 없으면 nosush저걸 예외로 나오는데 저 예외가 터지면 정상적으로 삭제가 실행되는 것으로 검증함.
//        Member deletedMember = repository.findById(member.getMemberId());

    } // 위 member와 findbyid로 찾은 member는 다름 findById에서는 sql로 반환된 값을 토대로 새로 객체를 만들어줬으니 다름.



}

//참고로 실행 결과에 member 객체의 참조 값이 아니라 실제 데이터가 보이는 이유는 롬복의 @Data 가
//toString() 을 적절히 오버라이딩 해서 보여주기 때문이다.
//isEqualTo() : findMember.equals(member) 를 비교한다. 결과가 참인 이유는 롬복의 @Data 는 해당
//객체의 모든 필드를 사용하도록 equals() 를 오버라이딩 하기 때문이다. ??



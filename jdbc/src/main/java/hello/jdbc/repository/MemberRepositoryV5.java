package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;


/**
 * 예외 누수 문제 해결
 * 체크 예외를 런타임 에외로 변경
 * MemberRepository 인터페이스 사용
 * throws SQLException 제거
 */

@Slf4j
public class MemberRepositoryV5 implements MemberRepository {

    private final JdbcTemplate template;

    public MemberRepositoryV5(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
    }

    @Override
    public Member save(Member member) {
        String sql = "INSERT INTO member(member_id, money) VALUES (?, ?)";
        template.update(sql, member.getMemberId(), member.getMoney());
        //아래 뭐 커넥션 가져오고 sql 삽입해주고 보내고, 예외처리까지 이 한줄로 다 마무리
        return member;

//        Connection con = null;
//        PreparedStatement pstmt = null; // 이것을 통해 쿼리를 보냄
//
//        try{
//            con = getConnection();
//            pstmt = con.prepareStatement(sql); // 데이터베이스에 전달할 SQL과 파라미터로 전달할 데이터들을 준비한다.
//            pstmt.setString(1, member.getMemberId()); //첫번째 ?에 들어감
//            pstmt.setInt(2, member.getMoney()); //두번째 ?에 들어감
//            pstmt.executeUpdate(); //Statement 를 통해 준비된 SQL을 커넥션을 통해 실제 데이터베이스에 전달한다.
//            return member;
//        } catch (SQLException e) {
//            throw exTranslator.translate("save", sql, e);
//        } finally {
//            close(con, pstmt, null); // 항상 sql 호출을 보장되기 위해 finally에서 쿼리를 실행하고 나면 리소스를 정리한다.
//        }


    }
    @Override
    public Member findById(String memberId) {
        String sql = "SELECT * FROM member WHERE member_id = ?"; // ?는 파라미터 바인딩
        // member 1건 조회할 때 queryForObject 활용.
        return template.queryForObject(sql, memberRowMapper(), memberId);
    }

    private RowMapper<Member> memberRowMapper() {
        return (rs, rowNum) -> { // RS => SQL 결과
            Member member = new Member();
            member.setMemberId(rs.getString("member_id"));
            member.setMoney(rs.getInt("money"));
            return member;
        };
    }

    @Override
    public void update(String memberId, int money)  {
        String sql = "UPDATE member SET money = ? WHERE member_id = ?";
        template.update(sql, money, memberId);

    }

    @Override
    public void delete(String memberId) {
        String sql = "DELETE FROM member WHERE member_id = ?";
        template.update(sql, memberId);

    }

//    아래 커넥션 닫는거, 동기화 모두 jdbc template이 해줌, 코드 간결과 중복 제거

    //쿼리를 실행하고 나면 리소스를 정리해야 한다. 여기서는 Connection , PreparedStatement 를 사용했다. 리소
    //스를 정리할 때는 항상 역순으로 해야한다.
    //스프링은 JDBC를 편리하게 다룰 수 있는 JdbcUtils 라는 편의 메서드를 제공한다.
    //JdbcUtils 을 사용하면 커넥션을 좀 더 편리하게 닫을 수 있다.
//    private void close(Connection con, Statement stmt, ResultSet rs){ // connection해제, 만약 con에서 close오류가 나면 pstmt로 못넘어감 이런 오류 해결하기 위해 따로 분리
//        JdbcUtils.closeResultSet(rs);
//        JdbcUtils.closeStatement(stmt);
//        //주의! 트랜잭션 동기롸를 사용하려면 DataSourceUtils를 사용해야 한다.
//        DataSourceUtils.releaseConnection(con, dataSource);// 이걸로 트랜잭션을 닫아줘야함.
////        JdbcUtils.closeConnection(con); //이걸 쓰면 안되고
////        DataSourceUtils.releaseConnection() 을 사용하면 커넥션을 바로 닫는 것이 아니다.
////                트랜잭션을 사용하기 위해 동기화된 커넥션은 커넥션을 닫지 않고 그대로 유지해준다.
////                트랜잭션 동기화 매니저가 관리하는 커넥션이 없는 경우 해당 커넥션을 닫는다.
//    }
//
//    private Connection getConnection() throws SQLException {
//        //주의! 트랜잭션 동기화를 사용하려면 DataSourceUtils를 사용해야 한다.
//        Connection con= DataSourceUtils.getConnection(dataSource);// dataSourceUtils에서 꺼내는 방식으로 변경 이러면 파라미터로 커넥션을 안넣어줘도 됨.
////        Connection con = dataSource.getConnection(); // 데이터 소스에서 직접 꺼내는 것이 아닌
//        log.debug("get connection={}, class={}", con, con.getClass());
//        return con; //connection 반환.
//    }

//get connection=HikariProxyConnection@xxxxxxxx1 wrapping conn0: url=jdbc:h2:...
//user=SA
//get connection=HikariProxyConnection@xxxxxxxx2 wrapping conn0: url=jdbc:h2:...
//user=SA
//get connection=HikariProxyConnection@xxxxxxxx3 wrapping conn0: url=jdbc:h2:...
//user=SA
//get connection=HikariProxyConnection@xxxxxxxx4 wrapping conn0: url=jdbc:h2:...
//user=SA
//get connection=HikariProxyConnection@xxxxxxxx5 wrapping conn0: url=jdbc:h2:...
//user=SA
//get connection=HikariProxyConnection@xxxxxxxx6 wrapping conn0: url=jdbc:h2:...
//user=SA
//```
//커넥션 풀 사용시 conn0 커넥션이 재사용 된 것을 확인할 수 있다.
//테스트는 순서대로 실행되기 때문에 커넥션을 사용하고 다시 돌려주는 것을 반복한다. 따라서 conn0 만 사용된
//다.
//웹 애플리케이션에 동시에 여러 요청이 들어오면 여러 쓰레드에서 커넥션 풀의 커넥션을 다양하게 가져가는 상황
//을 확인할 수 있다.

}

package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;


/**
 * 트랜잭션 - 트랜잭션 매니저
 * DataSourceUtils.getConnection()
 * DataSourceUtils.releaseConnection()
 */

@Slf4j
public class MemberRepositoryV3 {
    //datasource 주입
    private final DataSource dataSource;

    public MemberRepositoryV3(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Member save(Member member) throws SQLException {
        String sql = "INSERT INTO member(member_id, money) VALUES (?, ?)";

        Connection con = null;
        PreparedStatement pstmt = null; // 이것을 통해 쿼리를 보냄

        try{
            con = getConnection();
            pstmt = con.prepareStatement(sql); // 데이터베이스에 전달할 SQL과 파라미터로 전달할 데이터들을 준비한다.
            pstmt.setString(1, member.getMemberId()); //첫번째 ?에 들어감
            pstmt.setInt(2, member.getMoney()); //두번째 ?에 들어감
            pstmt.executeUpdate(); //Statement 를 통해 준비된 SQL을 커넥션을 통해 실제 데이터베이스에 전달한다.
            return member;
        } catch (SQLException e) {
            log.error("db error", e);
            throw e; //=> 밖으로 던져서 저기 위에 SQLException이 동작함
        } finally {
            close(con, pstmt, null); // 항상 sql 호출을 보장되기 위해 finally에서 쿼리를 실행하고 나면 리소스를 정리한다.
        }


    }

    public Member findById(String memberId) throws SQLException {
        String sql = "SELECT * FROM member WHERE member_id = ?"; // ?는 파라미터 바인딩

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);

            rs = pstmt.executeQuery(); // select 쿼리의 결과가 rs에 들어감,
            //현재는 1개만 조회서 그렇지만 원래는 데이터가 여러개니 while 문을 통해서 re.next로 반복문을 통해 조회해야함.
            if(rs.next()){ // re.next를 해줘야 데이터가 있는 곳 부터 시작됨.
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                //현재 커서가 가리키고 있는 위치의 member_id 데이터를 String 타입으로 반환한다
                member.setMoney(rs.getInt("money"));
                return member; // next에서 가져온 결과값을 다시 객체화 member로 변환.
            } else { // next 적용 시 데이터가 없을 때
                throw new NoSuchElementException("member not found");
            }
        } catch (SQLException e){
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, rs);
        }
    }

    //datasourceutils를 적용함으로써 이 conection 주입 방식은 필요가 없어짐.
//    public Member findById(Connection con, String memberId) throws SQLException {
//        String sql = "SELECT * FROM member WHERE member_id = ?"; // ?는 파라미터 바인딩
//
////        Connection con = null; 파라미터로 넘어온 connection을 써야함
//        PreparedStatement pstmt = null;
//        ResultSet rs = null;
//
//        try{
////            con = getConnection(); // 이걸 쓰면 안됨 파라미터로 넘어온 connection을 써야 커넥션 공유가 됨.
//            pstmt = con.prepareStatement(sql);
//            pstmt.setString(1, memberId);
//
//            rs = pstmt.executeQuery(); // select 쿼리의 결과가 rs에 들어감,
//            //현재는 1개만 조회서 그렇지만 원래는 데이터가 여러개니 while 문을 통해서 re.next로 반복문을 통해 조회해야함.
//            if(rs.next()){ // re.next를 해줘야 데이터가 있는 곳 부터 시작됨.
//                Member member = new Member();
//                member.setMemberId(rs.getString("member_id"));
//                //현재 커서가 가리키고 있는 위치의 member_id 데이터를 String 타입으로 반환한다
//                member.setMoney(rs.getInt("money"));
//                return member; // next에서 가져온 결과값을 다시 객체화 member로 변환.
//            } else { // next 적용 시 데이터가 없을 때
//                throw new NoSuchElementException("member not found");
//            }
//        } catch (SQLException e){
//            log.error("db error", e);
//            throw e;
//        } finally {
//            //connection은 여기서 닫지 않는다. => 서비스에서 닫아줘야함. 왜냐 커넥션은 service계층에서 커넥션을 넘겨주면 거기서 트랜잭션이 일어나기 떄문에
//            JdbcUtils.closeResultSet(rs);
//            JdbcUtils.closeStatement(pstmt);
//
//        }
//    }

    public void update(String memberId, int money) throws SQLException {
        String sql = "UPDATE member SET money = ? WHERE member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try{
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);
            int resultSize = pstmt.executeUpdate();
            log.info("resultSize={} ", resultSize); //쿼리를 실행하고 영향받은 row수를 반환함., 테이블 1개라 1반환됨.
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, null); // 항상 sql 호출을 보장되기 위해 finally에서 쿼리를 실행하고 나면 리소스를 정리한다.
        }
    }
        //datasourceutils를 적용함으로써 이 conection 주입 방식은 필요가 없어짐.
//    public void update(Connection con ,String memberId, int money) throws SQLException {
//        String sql = "UPDATE member SET money = ? WHERE member_id = ?";
//
//        PreparedStatement pstmt = null;
//
//        try{
//            pstmt = con.prepareStatement(sql);
//            pstmt.setInt(1, money);
//            pstmt.setString(2, memberId);
//            int resultSize = pstmt.executeUpdate();
//            log.info("resultSize={} ", resultSize); //쿼리를 실행하고 영향받은 row수를 반환함., 테이블 1개라 1반환됨.
//        } catch (SQLException e) {
//            log.error("db error", e);
//            throw e;
//        } finally {
//            JdbcUtils.closeStatement(pstmt);
//        }
//    }
//    1. 커넥션 유지가 필요한 두 메서드는 파라미터로 넘어온 커넥션을 사용해야 한다. 따라서 con =
//            getConnection() 코드가 있으면 안된다.
//2. 커넥션 유지가 필요한 두 메서드는 리포지토리에서 커넥션을 닫으면 안된다. 커넥션을 전달 받은 리포지토
//    리 뿐만 아니라 이후에도 커넥션을 계속 이어서 사용하기 때문이다. 이후 서비스 로직이 끝날 때 트랜잭션을
//    종료하고 닫아야 한다.

    public void delete(String memberId) throws SQLException {
        String sql = "DELETE FROM member WHERE member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try{
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {

            close(con, pstmt, null);
        }
    }

    //쿼리를 실행하고 나면 리소스를 정리해야 한다. 여기서는 Connection , PreparedStatement 를 사용했다. 리소
    //스를 정리할 때는 항상 역순으로 해야한다.
    //스프링은 JDBC를 편리하게 다룰 수 있는 JdbcUtils 라는 편의 메서드를 제공한다.
    //JdbcUtils 을 사용하면 커넥션을 좀 더 편리하게 닫을 수 있다.
    private void close(Connection con, Statement stmt, ResultSet rs){ // connection해제, 만약 con에서 close오류가 나면 pstmt로 못넘어감 이런 오류 해결하기 위해 따로 분리
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
        //주의! 트랜잭션 동기롸를 사용하려면 DataSourceUtils를 사용해야 한다.
        DataSourceUtils.releaseConnection(con, dataSource);// 이걸로 트랜잭션을 닫아줘야함.
//        JdbcUtils.closeConnection(con); //이걸 쓰면 안되고
//        DataSourceUtils.releaseConnection() 을 사용하면 커넥션을 바로 닫는 것이 아니다.
//                트랜잭션을 사용하기 위해 동기화된 커넥션은 커넥션을 닫지 않고 그대로 유지해준다.
//                트랜잭션 동기화 매니저가 관리하는 커넥션이 없는 경우 해당 커넥션을 닫는다.
    }

    private Connection getConnection() throws SQLException {
        //주의! 트랜잭션 동기화를 사용하려면 DataSourceUtils를 사용해야 한다.
        Connection con= DataSourceUtils.getConnection(dataSource);// dataSourceUtils에서 꺼내는 방식으로 변경 이러면 파라미터로 커넥션을 안넣어줘도 됨.
//        Connection con = dataSource.getConnection(); // 데이터 소스에서 직접 꺼내는 것이 아닌
        log.debug("get connection={}, class={}", con, con.getClass());
        return con; //connection 반환.
    }

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

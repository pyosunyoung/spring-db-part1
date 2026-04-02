package hello.jdbc.repository;

import hello.jdbc.connection.DBConnectionUtil;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.sql.*;
import java.util.NoSuchElementException;


/**
 * JDBC - DriverManager 사용
 */
@Slf4j
public class MemberRepositoryV0 {
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
    private void close(Connection con, Statement stmt, ResultSet rs){ // connection해제, 만약 con에서 close오류가 나면 pstmt로 못넘어감 이런 오류 해결하기 위해 따로 분리

        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) { // 여기서 예외가 터져도 여기서만 해당 부분이 끝나서 나머지 stmt, con은 닫아줄 수 있음.
                log.info("error", e);
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }

    }

    private Connection getConnection() throws SQLException {
        return DBConnectionUtil.getConnection();
    }

}

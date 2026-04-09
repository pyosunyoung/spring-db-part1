package hello.jdbc.exception.translator;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import static hello.jdbc.connection.ConnectionConst.*;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class SpringExceptionTranslatorTest {
    DataSource dataSource;

    @BeforeEach
    void init() {
        dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
    }

    @Test
    void sqlExceptionErrorCode(){
         String sql = "select bad grammer"; // sql 문법 잘못됨.

        try{
            Connection con = dataSource.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.executeQuery();
        } catch (SQLException e) {
            assertThat(e.getErrorCode()).isEqualTo(42122);
            int errorCode = e.getErrorCode();
            log.info("errorCode={}", errorCode);
            //org.h2.jdbc.JdbcSQLSyntaxErrorException
            log.info("error", e);
        }
    }

    @Test
    void exceptionTranslator(){
        String sql = "select bad grammer";
        try{
            Connection con = dataSource.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.executeQuery();
        }catch (SQLException e) {
            assertThat(e.getErrorCode()).isEqualTo(42122);
            //org.springframework.jdbc.support.sql-error-codes.xml

            SQLExceptionTranslator exTranslator = new SQLErrorCodeSQLExceptionTranslator();
            DataAccessException resultEx = exTranslator.translate("select", sql, e);
            //이렇게 하면 sql 예외를 분석해서 DataAccessException을 분석해줌., 알아서 예외 변환까지
            log.info("resultEx", resultEx);

            assertThat(resultEx.getClass()).isEqualTo(BadSqlGrammarException.class); //

        }
    }
}
//이전에 살펴봤던 SQL ErrorCode를 직접 확인하는 방법이다. 이렇게 직접 예외를 확인하고 하나하나 스프링이
//만들어준 예외로 변환하는 것은 현실성이 없다. 이렇게 하려면 해당 오류 코드를 확인하고 스프링의 예외 체계에
//맞추어 예외를 직접 변환해야 할 것이다. 그리고 데이터베이스마다 오류 코드가 다르다는 점도 해결해야 한다.
//그래서 스프링은 예외 변환기를 제공한다
// 즉 catch에서 스프링이 제공해주는 thorw new BadSqlGrammarException(e)로 변환해서 직접 던지는건 말이 안됨. _=> 그걸 스프링은 예외 변환기가 제공됨.

//데이터 접근 예외는 런타입 exception을 사용하기 위해 스프링  접근 계층을 통해 go
//SQL 오류같은것은 SQLException에 의존해야하는데 jpa로 변환시 이것도 전부 바꿔줘야함 그래서 스프링 변환기를 사용해서 (스프링 제공 데이터 접근)런타입으로 변환하여 해결(부모가 런타입?)
//스프링은 예외 변환기를 통해서 SQLException 의 ErrorCode 에 맞는 적절한 스프링 데이터 접근 예외로 변
//환해준다. 기존 MyDbeXCEPTION, mYdUPLICATEkEYeXCEP같은 직접 만드는 것들은 안해도 됨.
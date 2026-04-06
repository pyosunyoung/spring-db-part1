package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
@Slf4j
public class UncheckedAppTest {
    @Test
    void unchecked() {
        Controller controller = new Controller();
        assertThatThrownBy(() -> controller.request())
                .isInstanceOf(Exception.class);
    }

    @Test
    void printEx() {
        Controller controller = new Controller();
        try {
            controller.request();
        } catch (Exception e) {
            //e.printStackTrace(); SYSTEM OUT일 떈 이것
            log.info("ex", e); // 로그일 땐 이렇게 로그 확인. 실무는 여기 사용.
        }
    }

    static class Controller{
        Service service = new Service();

        public void request() throws SQLException, ConnectException {
            service.logic();
        }
    }

    static class Service {
        Repository repository = new Repository();
        NetworkClent networkClient = new NetworkClent();

        public void logic() {
            repository.call();
            networkClient.call();
        }
    }

    static class NetworkClent { //
        public void call() {
            throw new RuntimeConnectException("연결 실패");
        }
    }

    static class Repository {
        public void call()  { // check
            try {
                runSQL();
            } catch (SQLException e) {
                throw new RuntimeSQLException(e); // 체크 예외가 터지면 => 런타임 예외로 변환시켜서 넘긴다.
            } //이걸 반드시 예전 예외 SQLE예외를 넘겨줘야 이전에 어떤 예외가 터져서 RUNTIME으로 변경되어지는지 알 수 있어서
        } // 꼭 RUTIME(E)여기에 예전 예외를 집어 넣어줘야 함.

        //예외를 포함하지 않아서 기존에 발생한 java.sql.SQLException 과 스택 트레이스를 확인할 수 없다. 변환한
        //RuntimeSQLException 부터 예외를 확인할 수 있다. 만약 실제 DB에 연동했다면 DB에서 발생한 예외를 확인할
        //수 없는 심각한 문제가 발생한다.
        //예외를 전환할 때는 꼭! 기존 예외를 포함하자

        public void runSQL() throws SQLException {
            throw new SQLException("ex");
        }
    }

    static class RuntimeConnectException extends RuntimeException {
        public RuntimeConnectException(String message) {
            super(message);
        }
    }

    static class RuntimeSQLException extends RuntimeException {
        public RuntimeSQLException(Throwable cause) { // 이전 예외를 같이 넣을 수 있다?
            super(cause); // 즉 SQL => RUNTIME으로 예전 예외는 SQL
        }
    }
}

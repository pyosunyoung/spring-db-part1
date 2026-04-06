package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

@Slf4j
public class CheckedTest {

    @Test
    void checked_catch(){
        Service service = new Service();
        service.callCatch();
    }

    @Test
    void checked_throw(){
        Service service = new Service();
        assertThatThrownBy(() -> service.callThrow())
                .isInstanceOf(MyCheckedException.class);
    }

    /**
     * Exception을 상속받은 예외는 체크 예외가 된다.
     */

    static class MyCheckedException extends Exception {
        public MyCheckedException(String message) {
            super(message);


        }
    }
    /**
     * Checked 예외는
     * 예외를 잡아서 처리하거나, 던지거나 둘중 하나를 필수로 선택해야 한다.
     */

    static class Service{
        Repository repository = new Repository();
        /**
         * 예외를 잡아서 처리하는 코드
         */
        public void callCatch(){ // 예외를 잡아서 처리.
            try {
                repository.call(); // 예외 발생
            } catch (MyCheckedException e) { // exception e도 가능 부모이기 떄문. 근데 이렇게 하면 모든 자식 exception을 잡음
                //예외 처리 로직
                log.info("예외 처리, message={}", e.getMessage(), e);
            }
        }
        /**
         * 체크 예외를 밖으로 던지는 코드
         * 체크 예외는 예외를 잡지 않고 밖으로 던지려면 throws 예외를 메서드에 필수로 선언해야한
         다.
         */
        public void callThrow() throws MyCheckedException {
            repository.call();
        }
    }

    static class Repository {
        public void call() throws MyCheckedException { // 체크 예외는 밖으로
            throw new MyCheckedException("ex");
        }
    }
}

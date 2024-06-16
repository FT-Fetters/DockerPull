import com.heybcat.docker.pull.CommandStartup;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.locks.LockSupport;
import org.junit.Test;

public class TestStartup {

    @Test
    public void testWeb(){
        try {
            CommandStartup.main(new String[]{"--web"});
            LockSupport.park();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


}

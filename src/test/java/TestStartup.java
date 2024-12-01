import com.heybcat.docker.pull.CommandStartup;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.concurrent.locks.LockSupport;
import org.junit.Test;

public class TestStartup {

    @Test
    public void testWeb(){
        try {
            CommandStartup.main(new String[]{"--web"});
            LockSupport.park();
        } catch (URISyntaxException | InvocationTargetException | NoSuchMethodException | IllegalAccessException |
                 InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testCommand(){
        try {
            CommandStartup.main(new String[]{"bash:devel", "127.0.0.1", "7897"});
        } catch (URISyntaxException | InvocationTargetException | NoSuchMethodException | IllegalAccessException |
                 InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

}

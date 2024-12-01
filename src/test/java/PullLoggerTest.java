import com.heybcat.docker.pull.core.log.DefaultPullLogger;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class PullLoggerTest {

    @Test
    public void testLog(){
        DefaultPullLogger logger = new DefaultPullLogger();
        logger.msg("this is normal message");
        logger.warn("this is warning message");
        logger.err("this is error message");
        logger.debug("this is debug message");

    }

}

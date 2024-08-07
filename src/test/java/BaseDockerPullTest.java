import com.heybcat.docker.pull.core.pull.BaseDockerPull;
import com.heybcat.docker.pull.session.SessionManager;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BaseDockerPullTest {

    private static final Logger log = LoggerFactory.getLogger(BaseDockerPullTest.class);

    @Test
    void test() {
        String session = SessionManager.getInstance().newSession();
        new Thread(() -> BaseDockerPull.pull(null, "library", "nginx",
            "latest", "linux", "amd64", "127.0.0.1", 7890,
            session)).start();
        while (!SessionManager.getInstance().getSession(session).getStatus()
            .equals("finished")) {
            log.info("status: {} progress: {}",
                SessionManager.getInstance().getSession(session).getStatus(),
                SessionManager.getInstance().getSession(session).getProgress());
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }

}

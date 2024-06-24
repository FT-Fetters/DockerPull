

import com.heybcat.docker.pull.session.PullSessionManager;
import com.heybcat.docker.pull.session.PullSessionManager.PullSession;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class PullSessionManagerTest {

    private PullSessionManager pullSessionManager;

    @BeforeEach
    void setUp() {
        pullSessionManager = PullSessionManager.getInstance();
    }

    @Test
    void testNewSession() {
        String session = pullSessionManager.newSession();
        Assertions.assertNotNull(session);
        Assertions.assertEquals(16, session.length());

        // Ensure the generated session is unique
        String newSession = pullSessionManager.newSession();
        Assertions.assertNotEquals(session, newSession);
    }

    @Test
    void testGetSession() {
        String session = pullSessionManager.newSession();
        PullSession pullSession = pullSessionManager.getSession(session);
        Assertions.assertNotNull(pullSession);
        Assertions.assertEquals(session, pullSession.getSession());
    }

    @Test
    void testUpdateProgress() {
        String session = pullSessionManager.newSession();
        float progress = 0.5f;
        pullSessionManager.updateProgress(session, progress);

        PullSession pullSession = pullSessionManager.getSession(session);
        Assertions.assertEquals(progress, pullSession.getProgress(), 0.01);
    }

    @Test
    void testChangeStatus() {
        String session = pullSessionManager.newSession();
        String status = "completed";
        pullSessionManager.changeStatus(session, status);

        PullSession pullSession = pullSessionManager.getSession(session);
        Assertions.assertEquals(status, pullSession.getStatus());
    }
}

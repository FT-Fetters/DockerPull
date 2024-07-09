

import com.heybcat.docker.pull.session.SessionManager;
import com.heybcat.docker.pull.session.SessionManager.PullSession;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class SessionManagerTest {

    private SessionManager sessionManager;

    @BeforeEach
    void setUp() {
        sessionManager = SessionManager.getInstance();
    }

    @Test
    void testNewSession() {
        String session = sessionManager.newSession();
        Assertions.assertNotNull(session);
        Assertions.assertEquals(16, session.length());

        // Ensure the generated session is unique
        String newSession = sessionManager.newSession();
        Assertions.assertNotEquals(session, newSession);
    }

    @Test
    void testGetSession() {
        String session = sessionManager.newSession();
        PullSession pullSession = sessionManager.getSession(session);
        Assertions.assertNotNull(pullSession);
        Assertions.assertEquals(session, pullSession.getSession());
    }

    @Test
    void testUpdateProgress() {
        String session = sessionManager.newSession();
        float progress = 0.5f;
        sessionManager.updateProgress(session, progress);

        PullSession pullSession = sessionManager.getSession(session);
        Assertions.assertEquals(progress, pullSession.getProgress(), 0.01);
    }

    @Test
    void testChangeStatus() {
        String session = sessionManager.newSession();
        String status = "completed";
        sessionManager.changeStatus(session, status);

        PullSession pullSession = sessionManager.getSession(session);
        Assertions.assertEquals(status, pullSession.getStatus());
    }
}

package com.heybcat.docker.pull.session;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Fetters
 */
public class PullSessionManager {

    private static final PullSessionManager INSTANCE = new PullSessionManager();

    private static final int SESSION_LEN = 16;

    private final Map<String, PullSession> sessionMap;

    private final Random random;

    private PullSessionManager() {
        sessionMap = new ConcurrentHashMap<>();
        random = new Random(System.currentTimeMillis());
    }

    public static PullSessionManager getInstance() {
        return INSTANCE;
    }

    public String newSession() {
        String session = randomSession();
        while (sessionMap.containsKey(session)) {
            session = randomSession();
        }
        sessionMap.put(session, new PullSession(session));
        return session;
    }

    private String randomSession() {
        StringBuilder sb = new StringBuilder();
        for (int l = 0; l < SESSION_LEN; l++) {
            int i = random.nextInt(36);
            if (i > 9) {
                sb.append((char) (i + (97 - 10)));
            } else {
                sb.append((char) (48 + i));
            }
        }
        return sb.toString();
    }

    public PullSession getSession(String session) {
        return sessionMap.get(session);
    }

    public void updateProgress(String session, double progress) {
        PullSession pullSession = sessionMap.get(session);
        if (pullSession != null) {
            pullSession.progress = progress;
        }
    }

    public void changeStatus(String session, String status) {
        PullSession pullSession = sessionMap.get(session);
        if (pullSession != null) {
            pullSession.status = status;
        }
    }

    public void setResult(String session, String result){
        PullSession pullSession = sessionMap.get(session);
        if (pullSession != null) {
            pullSession.result = result;
        }
    }

    public static class PullSession {

        public PullSession(String session){
            this.session = session;
            this.progress = 0;
            this.status = "";
            this.result = null;
        }

        private String session;

        private double progress;

        private String status;

        private String result;

        public String getSession() {
            return session;
        }

        public double getProgress() {
            return progress;
        }

        public String getStatus() {
            return status;
        }

        public String getResult() {
            return result;
        }
    }


}

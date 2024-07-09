package com.heybcat.docker.pull.core.sftp;

import com.heybcat.docker.pull.session.SessionManager;
import com.heybcat.docker.pull.util.CryptoUtil;
import com.heybcat.docker.pull.web.config.GlobalConfig;
import com.heybcat.docker.pull.web.entity.view.UploadImageView;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ldqc.tightcall.util.StringUtil;

/**
 * @author Fetters
 */
public class SftpUploader {

    private SftpUploader() {
        throw new UnsupportedOperationException();
    }

    private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(
        4, 4, 0L, java.util.concurrent.TimeUnit.MILLISECONDS,
        new java.util.concurrent.LinkedBlockingQueue<>(20),
        r -> new Thread(r, "sftp-uploader" + r.hashCode())
    );
    private static final Logger log = LoggerFactory.getLogger(SftpUploader.class);

    public static UploadImageView upload(File imageFile, String targetPath) {
        String sshHost = GlobalConfig.getSshHost();
        String sshPort = GlobalConfig.getSshPort();
        String sshUser = GlobalConfig.getSshUser();
        String sshPassword = CryptoUtil.moduloDecrypt(GlobalConfig.getSshPassword());
        if (StringUtil.isAnyBlank(sshHost, sshUser, sshPassword)) {
            return UploadImageView.fail("missing config");
        }

        String session = SessionManager.getInstance().newSession();

        EXECUTOR.execute(
            () -> {

                ChannelSftp channelSftp;
                try (FileInputStream fis = new FileInputStream(imageFile)) {
                    channelSftp = buildChannelSftp(sshHost, Integer.valueOf(sshPort), sshUser, sshPassword);
                    channelSftp.cd(targetPath);

                    final AtomicInteger lastProgress = new AtomicInteger(0);

                    channelSftp.put(fis, imageFile.getName(), new SftpProgressMonitor() {
                        private long transferred = 0;

                        private final long fileSize = imageFile.length();


                        @Override
                        public void init(int i, String src, String dest, long l) {
                            log.info("upload image {} to server {} path {}", imageFile.getName(), sshHost, targetPath);
                        }

                        @Override
                        public boolean count(long l) {
                            transferred += l;
                            double percent = ((double) transferred * 100) / fileSize;
                            if (((int) percent) > lastProgress.get()) {
                                SessionManager.getInstance().updateProgress(session, percent);
                                SessionManager.getInstance().changeStatus(session, "uploading");
                                log.info("upload image {} to server {} path {} progress {}%", imageFile.getName(),
                                    sshHost, targetPath, percent);
                                lastProgress.set((int) percent);
                            }
                            return true;
                        }

                        @Override
                        public void end() {
                            SessionManager.getInstance().changeStatus(session, "finished");
                            log.info("upload image {} to server {} path {} finished", imageFile.getName(), sshHost,
                                targetPath);
                        }
                    });

                    if (GlobalConfig.isLoadImage()) {
                        loadToDocker(targetPath, imageFile.getName(),
                            buildChannelExec(sshHost, Integer.valueOf(sshPort), sshUser, sshPassword));
                    }

                } catch (JSchException | SftpException | IOException e) {
                    SessionManager.getInstance().setResult(session, e.getMessage());
                    SessionManager.getInstance().changeStatus(session, "fail");
                    log.error("image upload fail", e);
                }
            }
        );
        return new UploadImageView(session, true, "uploading");
    }

    private static void loadToDocker(String imagePath, String imageFileName, ChannelExec channelExec) {
        channelExec.setCommand("docker load < " + imagePath + "/" + imageFileName);
        channelExec.setErrStream(System.err);
        channelExec.setInputStream(null);
        try {
            channelExec.connect();
            InputStream input = channelExec.getInputStream();
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(input));
            String inputLine;
            while ((inputLine = inputReader.readLine()) != null) {
                log.info("{}@{}> {}", GlobalConfig.getSshUser(), GlobalConfig.getSshHost(), inputLine);
            }
        } catch (IOException | JSchException e) {
            log.error("load image to docker fail", e);
        }

    }


    private static ChannelSftp buildChannelSftp(String sshHost, Integer sshPort, String sshUser, String sshPassword)
        throws JSchException {
        Session session = sshConnect(sshHost, sshPort, sshUser, sshPassword);

        Channel channel = session.openChannel("sftp");
        channel.connect();
        return ((ChannelSftp) channel);
    }

    private static ChannelExec buildChannelExec(String sshHost, Integer sshPort, String sshUser, String sshPassword)
        throws JSchException {
        Session session = sshConnect(sshHost, sshPort, sshUser, sshPassword);

        Channel channel = session.openChannel("exec");
        return (ChannelExec) channel;
    }

    private static Session sshConnect(String sshHost, Integer sshPort, String sshUser, String sshPassword)
        throws JSchException {
        JSch jSch = new JSch();
        Session session = jSch.getSession(sshUser, sshHost, sshPort);
        session.setPassword(sshPassword);

        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);

        session.connect();
        return session;
    }

}

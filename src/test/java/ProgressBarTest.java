import com.heybcat.docker.pull.core.log.ConsoleStyle;
import com.heybcat.docker.pull.core.log.PullProgressBar;
import java.util.Random;
import org.junit.Test;

public class ProgressBarTest {



    @Test
    public void run(){
        char incomplete = '░'; // U+2591 Unicode Character 表示还没有完成的部分
        char complete = '█'; // U+2588 Unicode Character 表示已经完成的部分
        int  total = 100;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < total; i++) {
            builder.replace(i, i + 1, String.valueOf(complete));
            String progressBar = "\r" + builder;
            String percent = " " + (i + 1) + "%";
            System.out.print(progressBar + percent);
            try {
                Thread.sleep(i * 5L);
            } catch (InterruptedException ignored) {

            }
        }
    }

    @Test
    public void runMy(){

        PullProgressBar bar = PullProgressBar.create(1000, "B", "layer:", true);

        int j = 0;
        Random random = new Random(System.currentTimeMillis());
        for (int i = 1; i <= 1000; i++) {
            bar.updateAndPrint(i);
            j++;
            try {
                Thread.sleep(10 - random.nextInt(5));
            } catch (InterruptedException ignored) {

            }
        }

    }

    @Test
    public void testMutipleProgressBar(){
        Thread t1 = new Thread(() -> {
            PullProgressBar bar = PullProgressBar.create(1024, "B", "layer1:", true);
            int j = 0;
            for (int i = 0; i < 1024; i += j) {
                bar.updateAndPrint(i);
                j++;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {

                }
            }
        });

        Thread t2 = new Thread(() -> {
            PullProgressBar bar = PullProgressBar.create(1024, "B", "layer2:", true);
            int j = 0;
            for (int i = 0; i < 1024; i += j) {
                bar.updateAndPrint(i);
                j++;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {

                }
            }
        });
        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testConsoleStyle(){
        String s1 = "aaaaaa";
        String s2 = "bbb";
        String s3 = "c";
        String s4 = "ddddd";

        String s = ConsoleStyle.green(s1) + ConsoleStyle.red(s2) + ConsoleStyle.yellow(s3) + ConsoleStyle.green(s4);
        System.out.println(ConsoleStyle.underlinePercent(s, 100, 50));
    }

}

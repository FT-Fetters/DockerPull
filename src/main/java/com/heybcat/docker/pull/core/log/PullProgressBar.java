package com.heybcat.docker.pull.core.log;

/**
 * @author Fetters
 */
public class PullProgressBar {

    private static final String[] PROGRESS_BAR_CHARS = new String[]{"⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"};

    private final long total;

    private final String unit;

    private final boolean showSpeed;

    private final String prefix;

    private final long startTime;

    private PullProgressBar(long total, String unit, String prefix, boolean showSpeed) {
        this.total = total;
        this.unit = unit;
        this.showSpeed = showSpeed;
        this.prefix = prefix;
        this.startTime = System.currentTimeMillis();
    }

    public static PullProgressBar create(long total) {
        return new PullProgressBar(total, "", "", false);
    }

    public static PullProgressBar create(long total, String unit) {
        return new PullProgressBar(total, unit, "", false);
    }

    public static PullProgressBar create(long total, String unit, String prefix){
        return new PullProgressBar(total, unit, prefix, false);
    }

    public static PullProgressBar create(long total, String unit, String prefix, boolean showSpeed){
        return new PullProgressBar(total, unit, prefix, showSpeed);
    }

    public void updateAndPrint(long current) {
        // 确保当前值不超过总量
        current = Math.min(current, total);

        // 构造进度条输出
        StringBuilder builder = new StringBuilder();

        // 添加前缀、当前值、总值以及单位
        builder.append(prefix.endsWith(" ") ? prefix : prefix + " ")
            .append(current)
            .append(unit)
            .append("/")
            .append(total)
            .append(unit)
            .append(" ");

        // 显示速度信息（如果启用）
        if (showSpeed) {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - startTime;

            // 避免除以零的情况
            long speed = elapsedTime > 0
                ? (long) ((float) current / elapsedTime * 1000.0f)
                : 0;

            builder.append(ConsoleStyle.yellow("⚡"))
                .append(speed)
                .append(unit)
                .append("/s");
        }

        // 添加最后的符号，若完成则显示 √，否则显示动态字符
        builder.append(" ");
        if (current == total) {
            builder.append(ConsoleStyle.green("√"));
        } else {
            builder.append(PROGRESS_BAR_CHARS[(int) (current % PROGRESS_BAR_CHARS.length)]);
        }

        // 打印到控制台
        if (current == total){
            System.out.println("\r" + builder + " " + String.format("[%ds]", (System.currentTimeMillis() - startTime) / 1000));
        }else {
            System.out.print("\r" + ConsoleStyle.underlinePercent(builder.toString(), total, current));
        }
        System.out.flush();
    }


}

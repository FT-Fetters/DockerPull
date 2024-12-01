package com.heybcat.docker.pull.core.log;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Fetters
 */
public class ConsoleStyle {
    private static final String STYLE_PATTERN = "\\033\\[[;\\d]*m";


    private ConsoleStyle(){}

    public static String green(String text){
        return "\033[32m" + text + "\033[0m";
    }

    public static String red(String text){
        return "\033[31m" + text + "\033[0m";
    }

    public static String yellow(String text){
        return "\033[33m" + text + "\033[0m";
    }

    public static String blue(String text){
        return "\033[34m" + text + "\033[0m";
    }

    public static String underline(String text){
        return "\033[4m" + text + "\033[0m";
    }

    public static String underlinePercent(String text, long total, long current){
        if (StringUtils.isBlank(text)){
            return text;
        }
        int charCount = text.replaceAll(STYLE_PATTERN, "").length();
        // calculate percentage
        float percent = (float)current / total;
        Pattern pattern = Pattern.compile(STYLE_PATTERN);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()){
            return underlinePercentWithStyle(text, matcher, charCount, percent);
        }
        String underlinePart = text.substring(0, (int)(percent * charCount));
        String remainPart = text.substring((int)(percent * charCount));
        return underlinePart + remainPart;

    }

    private static String underlinePercentWithStyle(String text, Matcher matcher, int charCount, float percent) {
        int preEnd = 0;
        List<String> partList = new ArrayList<>();
        do {
            String group = matcher.group();
            if (matcher.start() > preEnd){
                partList.add(text.substring(preEnd, matcher.start()));
            }
            partList.add(group);
            preEnd = matcher.end();
        }while (matcher.find());
        partList.add(text.substring(preEnd));
        partList = partList.stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
        int stepCharCount = 0;
        int underlineEnd = (int)(charCount * percent);
        String preStyle = "";
        for (int i = 0; i < partList.size(); i++) {
            String part = partList.get(i);
            if (part.matches(STYLE_PATTERN)) {
                if (stepCharCount < underlineEnd && part.contains("\033[0m")){
                    partList.set(i, part + "\033[4m");
                }
                preStyle = part;
                continue;
            }
            if (part.length() + stepCharCount < underlineEnd){
                stepCharCount += part.length();
                continue;
            }
            int underlineLen = underlineEnd - stepCharCount;
            partList.set(i, part.substring(0, underlineLen)  + "\033[0m" + preStyle + part.substring(underlineLen));
            break;
        }
        return "\033[4m" + StringUtils.join(partList, "");
    }

}

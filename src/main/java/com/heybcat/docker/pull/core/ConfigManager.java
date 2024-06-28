package com.heybcat.docker.pull.core;

import com.heybcat.docker.pull.web.config.Config;
import com.heybcat.docker.pull.web.config.GlobalConfig;
import com.heybcat.tightlyweb.common.util.FileUtil;
import com.heybcat.tightlyweb.common.util.ReflectionUtil;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Fetters
 */
public class ConfigManager {

    private ConfigManager() {

    }

    public static void load()
        throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Path runPath = Paths.get(new File("").getAbsolutePath());
        File configFile = new File(runPath.toAbsolutePath() + "/config");
        if (!configFile.exists()) {
            return;
        }

        String configFileContent = FileUtil.readFile(configFile.getAbsolutePath());
        // split as lines
        String[] lines = configFileContent.split("\n");

        Map<String, String> configMap = new HashMap<>(lines.length);

        for (String line : lines) {
            String[] keyWithValue = line.split(":");
            if (keyWithValue.length != 2) {
                continue;
            }
            configMap.put(keyWithValue[0], keyWithValue[1]);
        }

        Field[] declaredFields = GlobalConfig.class.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            Config config = declaredField.getAnnotation(Config.class);
            if (config == null){
                continue;
            }
            String key = config.value();
            String value = configMap.get(key);
            if (value == null){
                continue;
            }
            Method method = ReflectionUtil.setterMethod(GlobalConfig.class, declaredField);
            method.invoke(GlobalConfig.class, value);
        }
    }

}

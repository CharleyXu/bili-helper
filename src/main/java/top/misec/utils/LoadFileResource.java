package top.misec.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author Junzhou Liu
 * @create 2020/10/17 19:31
 * 工具类通过流的方式读取文件
 */
@Slf4j
public class LoadFileResource {

    public static String loadConfigJsonFromFile() {
        return loadConfigJsonFromFile("config.json");
    }

    public static String[] loadUserInfoFromFile() {
        return loadConfigJsonFromFile("user.info").split(" ");
    }

    /**
     * 获取包路径
     */
    private static String getJarPath() {
        return getJarPath(getJarFullPath(LoadFileResource.class));
    }

    private static String getJarPath(String path) {
        if (Objects.isNull(path)) {
            return "";
        }
        int bootInf = path.lastIndexOf("/BOOT-INF");
        if (bootInf > 0) {
            path = path.substring(0, bootInf);
        }
        int jarIndex = path.lastIndexOf(".jar");
        //  jar运行和单元测试区分
        if (jarIndex > 0) {
            path = path.substring(0, jarIndex);
            int lastSlashIndex = path.lastIndexOf("/");
            path = path.substring(0, lastSlashIndex + 1);
        } else if (path.endsWith("test-classes/")) {
            int pos = path.lastIndexOf("target");
            path = path.substring(0, pos);
            path = path + "src/test/resources/";
        } else {
            int pos = path.lastIndexOf("target");
            path = path.substring(0, pos);
            path = path + "src/main/resources/";
        }
        return path;
    }

    private static String getJarFullPath(Class clazz) {
        String path = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
        try {
            path = URLDecoder.decode(path, "UTF-8");
            path = path.replace("\\", "/");
            if (path.contains(":")) {
                // 删去 file:
                path = path.substring(5);
            }
            return path;
        } catch (UnsupportedEncodingException e) {
            log.error("[FileUtil] get JarFullPath, error message: {}", e.getMessage(), e);
        }
        return "";
    }

    /**
     * 从外部资源读取配置文件
     *
     * @return config
     */
    private static String loadConfigJsonFromFile(String fileName) {
        String config = "";
        String outPath = getJarPath() + fileName + File.separator;
        try (InputStream is = new FileInputStream(outPath)) {
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            config = new String(buffer, StandardCharsets.UTF_8);
        } catch (FileNotFoundException e) {
            log.info("未扫描到外部配置文件，即将加载默认配置文件【此提示仅针自行部署的Linux用户，普通用户请忽略】");
        } catch (IOException e) {
            e.printStackTrace();
            log.debug("", e);
        }
        return config;
    }


    /**
     * 从resource读取版本文件
     *
     * @param fileName 文件名
     * @return 返回读取到文件
     */
    public static String loadJsonFromAsset(String fileName) {
        String json = null;
        try (InputStream is = LoadFileResource.class.getClassLoader().getResourceAsStream(fileName);
        ) {
            assert is != null;
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            log.debug("", e);
        }
        return json;
    }


    /**
     * @param filePath 读入的文件路径
     * @return 返回str
     */
    public static String loadFile(String filePath) {
        String logs = null;
        try (InputStream is = new FileInputStream(filePath)) {
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            logs = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            log.debug("", e);
        }
        return logs;
    }
}

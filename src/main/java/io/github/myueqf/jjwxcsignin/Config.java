package io.github.myueqf.jjwxcsignin;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public final class Config {
    private static final Logger LOGGER = LoggerFactory.getLogger("Config-JJWXC-Sign-in");
    private Config() {}

    // --- 播报设置 ---
    public static String TARGET_PLAYER_USERNAME;

    // --- API & 认证 ---
    public static String SIGNIN_TOKEN;
    public static final String SIGN_URL = "https://android.jjwxc.net/androidapi/signin";

    // --- 加密密钥 ---
    public static final String KEY_HEX = "4b573844766d324e";
    public static final String IV_HEX = "3161653263393462";

    // --- HTTP 请求头 ---
    public static final String USER_AGENT = "Mozilla/5.0 (Linux; Android 15)";
    public static final String SM_DEVICE_ID = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String VERSION_CODE = "423";

    static {
        File configFile = FabricLoader.getInstance().getConfigDir().resolve("jjwxcsignin.txt").toFile();
        Properties properties = new Properties();

        // 检查配置文件是否存在
        if (!configFile.exists()) {
            LOGGER.info("配置文件似乎不存在。。。");
            createDefaultConfig(configFile);
        }

        // 加载配置～
        try (FileReader reader = new FileReader(configFile)) {
            properties.load(reader);
        } catch (IOException e) {
            LOGGER.error("加载配置文件失败QAQ", e);
        }

        TARGET_PLAYER_USERNAME = properties.getProperty("TARGET_PLAYER_USERNAME", "lanzhan5656");
        SIGNIN_TOKEN = properties.getProperty("SIGNIN_TOKEN", "");
    }

    /**
     * 创建默认配置文件并写入初始值。
     * @param configFile 要创建的 File 对象。
     */
    private static void createDefaultConfig(File configFile) {
        Properties defaultProperties = new Properties();
        defaultProperties.setProperty("TARGET_PLAYER_USERNAME", "lanzhan5656");
        defaultProperties.setProperty("SIGNIN_TOKEN", "在这里粘贴你的Token");

        try (FileWriter writer = new FileWriter(configFile)) {
            defaultProperties.store(writer, "Configuration for JJWXC Sign-in Script");
            LOGGER.info("新的配置文件创建成功QwQ：{}", configFile.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.error("创建配置文件失败QAQ", e);
        }
    }
}
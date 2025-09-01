package io.github.myueqf.jjwxcsignin;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class main implements ModInitializer {
    public static final String MOD_ID = "JJWXC-Sign-in";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private final JjwxcSignInService signInService = new JjwxcSignInService();

    @Override
    public void onInitialize() {
        LOGGER.info("晋江文学城签到脚本正在初始化～");
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            LOGGER.info("    猫猫探头～");
            LOGGER.info("(=^-ω-^=)   ");
            LOGGER.info("============");
            LOGGER.info("准备执行晋江签到啦～");
            try {
                signInService.performSignIn();
            } catch (Exception e) {
                LOGGER.error("签到失败，发生异常QAQ", e);
            }
            scheduleDailySignIn(); // 定时任务～
        });

        // 玩家加入游戏事件～
        registerPlayerJoinListener();
    }

    private void scheduleDailySignIn() {
        LOGGER.info("初始化定时任务～");
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextExecutionTime;

        // 设置目标时间～
        nextExecutionTime = now.withHour(7).withMinute(24).withSecond(0).withNano(0);

        // 如果时间已超过目标时间，则推迟到明天嗷～
        if (now.isAfter(nextExecutionTime)) {
            nextExecutionTime = nextExecutionTime.plusDays(1);
        }

        long initialDelay = Duration.between(now, nextExecutionTime).toMillis();
        scheduler.scheduleAtFixedRate(() -> {
            LOGGER.info("晋江每日签到～");
            try {
                signInService.performSignIn();
            } catch (Exception e) {
                LOGGER.error("签到失败，发生异常QAQ", e);
            }
        }, initialDelay, 24 * 60 * 60 * 1000, TimeUnit.MILLISECONDS);
    }


    private void registerPlayerJoinListener() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            String playerName = handler.player.getGameProfile().getName();
            if (Config.TARGET_PLAYER_USERNAME.equalsIgnoreCase(playerName)) {
                LOGGER.info("目标玩家 {} 已加入游戏，发送上次签到结果～", playerName);
                SignInResult result = JjwxcSignInService.getLastSignInResult();

                handler.player.sendMessage(Text.literal("§e[晋江签到结果]"), false);
                handler.player.sendMessage(Text.literal("§a-> 签到状态：" + result.status()), false);

                // 仅当签到成功时才显示月石和天数信息嗷～
                if (result.successful()) {
                    handler.player.sendMessage(Text.literal("§a-> 获得月石数量：" + result.coins() + "个"), false);
                    handler.player.sendMessage(Text.literal("§a-> 连续签到天数：" + result.signDays() + "天"), false);
                }
            }
        });
    }
}

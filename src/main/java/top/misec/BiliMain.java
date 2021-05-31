package top.misec;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import lombok.extern.slf4j.Slf4j;
import top.misec.config.Config;
import top.misec.login.ServerVerify;
import top.misec.login.Verify;
import top.misec.task.DailyTask;
import top.misec.task.ServerPush;
import top.misec.utils.LoadFileResource;
import top.misec.utils.VersionInfo;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Junzhou Liu
 * @create 2020/10/11 2:29
 */
@Slf4j
public class BiliMain {

    public static void main(String[] args) {
        log.info("开始轻松获取B站经验值");
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1, new ThreadPoolExecutor.AbortPolicy());
        scheduledThreadPoolExecutor.scheduleAtFixedRate(() -> execute(buildArray(args)), initialDelay(), TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
    }

    private static String[] buildArray(String[] args) {
        return args == null || args.length < 3 ? LoadFileResource.loadUserInfoFromFile() : args;
    }

    private static void execute(String[] args) {
        if (args.length < 3) {
            log.info("任务启动失败");
            log.warn("Cookies参数缺失，请检查是否在Github Secrets中配置Cookies参数");
            return;
        }
        //读取环境变量
        Verify.verifyInit(args[0], args[1], args[2]);

        if (args.length > 4) {
            ServerVerify.verifyInit(args[3], args[4]);
        } else if (args.length > 3) {
            ServerVerify.verifyInit(args[3]);
        }
        VersionInfo.printVersionInfo();
        //每日任务65经验
        Config.getInstance().configInit();
        if (!Boolean.TRUE.equals(Config.getInstance().getSkipDailyTask())) {
            DailyTask dailyTask = new DailyTask();
            dailyTask.doDailyTask();
        } else {
            log.info("已开启了跳过本日任务，本日任务跳过（不会发起任何网络请求），如果需要取消跳过，请将skipDailyTask值改为false");
            ServerPush.doServerPush();
        }
    }

    private static long initialDelay() {
        ExecutionTime executionTime = ExecutionTime.forCron(new CronParser(CronDefinitionBuilder
                .instanceDefinitionFor(CronType.QUARTZ)).parse("0 10 16 * * ?"));
        long epochSecond = executionTime.nextExecution(ZonedDateTime.now(ZoneId.systemDefault()))
                .orElse(ZonedDateTime.now(ZoneId.systemDefault())).toEpochSecond();
        return epochSecond - Instant.now().getEpochSecond();
    }

}

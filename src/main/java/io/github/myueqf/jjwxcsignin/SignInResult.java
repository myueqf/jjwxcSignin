package io.github.myueqf.jjwxcsignin;

/**
 * 数据记录～
 * @param successful 签到是否成功。
 * @param status 签到状态消息
 * @param coins 收到的月石数量。
 * @param signDays 连续签到天数。
 */
public record SignInResult(boolean successful, String status, String coins, String signDays) {

    /**
     * @param status 失败信息。
     * @return 代表失败的 SignInResult 新实例。
     */
    public static SignInResult failure(String status) {
        return new SignInResult(false, status, "N/A", "N/A");
    }

    /**
     * @param status 成功信息。
     * @param coins 收到的月石。
     * @param signDays 连续签到天数。
     * @return 代表成功的 SignInResult 新实例。
     */
    public static SignInResult success(String status, String coins, String signDays) {
        return new SignInResult(true, status, coins, signDays);
    }
}

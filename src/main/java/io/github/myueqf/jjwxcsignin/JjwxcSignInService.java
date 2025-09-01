package io.github.myueqf.jjwxcsignin;

import org.json.JSONObject;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * 发送处理签到请求～
 */
public class JjwxcSignInService {

    private final HttpClient httpClient;
    // 将用于存储结果的变量类型从 String 更改为 SignInResult
    private static SignInResult lastSignInResult = SignInResult.failure("木有执行签到。");

    public JjwxcSignInService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * 获取上次签到的结果。
     * @return 包含上次签到详情的 SignInResult 对象。
     */
    public static SignInResult getLastSignInResult() {
        return lastSignInResult;
    }

    /**
     * 执行完整的签到流程。
     * @throws Exception 如果流程中的任何步骤失败。
     */
    public void performSignIn() throws Exception {
        long timestampMillis = System.currentTimeMillis();
        long timestampSeconds = timestampMillis / 1000;
        String token = Config.SIGNIN_TOKEN;

        // 1. 为请求体和请求头准备加密数据
        String signPayload = timestampMillis + ":" + token;
        String encryptedSign = EncryptionUtil.desEncrypt(Config.KEY_HEX, Config.IV_HEX, signPayload);

        String headerPayload = String.format("{\"token\":\"%s\",\"time\":%d}", token, timestampSeconds);
        String encryptedHeaderSign = EncryptionUtil.desEncrypt(Config.KEY_HEX, Config.IV_HEX, headerPayload);

        // 2. 构建 HTTP POST 请求
        HttpRequest request = buildRequest(encryptedSign, encryptedHeaderSign);

        // 3. 发送请求并获取响应
        try {
            main.LOGGER.info("正在向晋江API发送签到请求...");
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            main.LOGGER.info("晋江API响应体: {}", response.body());

            handleApiResponse(response.body());

        } catch (Exception e) {
            main.LOGGER.error("发送签到请求时发生错误。", e);
            throw e;
        }
    }

    private HttpRequest buildRequest(String encryptedSign, String encryptedHeaderSign) {
        String requestBody = "versionCode=" + Config.VERSION_CODE + "&sign=" + URLEncoder.encode(encryptedSign, StandardCharsets.UTF_8);

        return HttpRequest.newBuilder()
                .uri(URI.create(Config.SIGN_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("User-Agent", Config.USER_AGENT)
                .header("sign", encryptedHeaderSign)
                .header("SMDeviceID", Config.SM_DEVICE_ID)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
    }


    /**
     * 解析API的JSON响应～
     * @param responseBody API返回的JSON字符串。
     */
    private void handleApiResponse(String responseBody) {
        try {
            JSONObject json = new JSONObject(responseBody);
            int code = json.optInt("code", -1);
            String message = json.optString("message", "未找到消息。");
            String formattedMessage = message.replace("\\u7b7e\\u5230\\u6210\\u529f", "签到成功");

            if (code == 200) {
                JSONObject data = json.optJSONObject("data");
                if (data != null) {
                    String coins = data.optString("coins", "N/A");
                    String signdays = data.optString("signdays", "N/A");

                    main.LOGGER.info("晋江签到结果：");
                    main.LOGGER.info("-> 签到状态：{}", formattedMessage);
                    main.LOGGER.info("-> 获得月石数量：{}个", coins);
                    main.LOGGER.info("-> 连续签到天数：{}天", signdays);

                    lastSignInResult = SignInResult.success(formattedMessage, coins, signdays);

                } else {
                    main.LOGGER.warn("签到响应成功，但未找到'data'字段。");
                    lastSignInResult = SignInResult.failure("签到成功，但响应中缺少数据。");
                }
            } else {
                main.LOGGER.error("签到失败: {}", message);
                lastSignInResult = SignInResult.failure(message);
            }
        } catch (Exception e) {
            main.LOGGER.error("解析API响应时出错。", e);
            lastSignInResult = SignInResult.failure("解析API响应时出错。");
        }
    }
}

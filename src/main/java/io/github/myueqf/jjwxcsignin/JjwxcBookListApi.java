package io.github.myueqf.jjwxcsignin;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 获取晋江限免文列表～
 */

public final class JjwxcBookListApi {
    /**
     * 对应JSON响应的根结构。
     */
    public static class BookListRoot {
        public final String code;
        public final String message;
        public final BookListData data;

        /**
         * 通过解析JSONObject来构造实例。
         * @param json API返回的根JSONObject。
         */
        public BookListRoot(JSONObject json) {
            this.code = json.optString("code");
            this.message = json.optString("message");
            JSONObject dataObject = json.optJSONObject("data");
            this.data = (dataObject != null) ? new BookListData(dataObject) : null;
        }
    }

    /**
     * 对应JSON中的 "data" 对象，包含主要的业务数据。
     */
    public static class BookListData {
        public final String channelid;
        public final int naturalRankId;
        public final List<NovelData> novels;
        public final String channelRule;
        public final String channelRuleDown;

        /**
         * 通过解析 "data" JSONObject来构造实例。
         * @param json "data"部分的JSONObject。
         */
        public BookListData(JSONObject json) {
            this.channelid = json.optString("channelid");
            this.naturalRankId = json.optInt("natural_rank_id");
            this.channelRule = json.optString("channelRule");
            this.channelRuleDown = json.optString("channelRuleDown");

            this.novels = new ArrayList<>();
            JSONArray novelsArray = json.optJSONArray("data");
            if (novelsArray != null) {
                for (int i = 0; i < novelsArray.length(); i++) {
                    JSONObject novelObject = novelsArray.optJSONObject(i);
                    if (novelObject != null) {
                        this.novels.add(new NovelData(novelObject));
                    }
                }
            }
        }
    }

    /**
     * 对应每本小说的数据结构。
     */
    public static class NovelData {
        public final String novelId;
        public final String novelName;
        public final String authorId;
        public final String authorName;
        public final String cover;
        public final String local;
        public final String localImg;
        public final String novelIntroShortLower;
        public final String novelIntroShortUpper;
        public final String novelIntro;
        public final String novelStep;
        public final String tags;
        public final String freeDate;
        public final String nowFree;
        public final String novelSize;
        public final String novelSizeformat;
        public final String novelClass;
        public final String isVipMonth;
        public final String recommendInfo;

        /**
         * 通过解析单个小说的JSONObject来构造实例。
         * @param json 数组中代表单个小说的JSONObject。
         */
        public NovelData(JSONObject json) {
            this.novelId = json.optString("novelId");
            this.novelName = json.optString("novelName");
            this.authorId = json.optString("authorId");
            this.authorName = json.optString("authorName");
            this.cover = json.optString("cover");
            this.local = json.optString("local");
            this.localImg = json.optString("localImg");
            this.novelIntroShortLower = json.optString("novelIntroshort");
            this.novelIntroShortUpper = json.optString("novelIntroShort");
            this.novelIntro = json.optString("novelIntro");
            this.novelStep = json.optString("novelStep");
            this.tags = json.optString("tags");
            this.freeDate = json.optString("freeDate");
            this.nowFree = json.optString("nowFree");
            this.novelSize = json.optString("novelSize");
            this.novelSizeformat = json.optString("novelSizeformat");
            this.novelClass = json.optString("novelClass");
            this.isVipMonth = json.optString("isVipMonth");
            this.recommendInfo = json.optString("recommendInfo");
        }
    }

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private JjwxcBookListApi() {}

    /**
     * 获取晋江今日免费书籍列表。
     *
     * @return 包含书籍列表信息的 BookListRoot 对象。
     * @throws IOException 如果发生网络错误。
     * @throws InterruptedException 如果线程在HTTP请求期间被中断。
     */
    public static BookListRoot getBooksList() throws IOException, InterruptedException {
        LocalDate today = LocalDate.now();
        String date = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // 构建API请求参数中的 channelBody JSON字符串
        String channelBody = String.format("{\"date_free_%s\":{\"offset\":\"0\",\"limit\":\"40\"}}", date);

        String escapedChannelBody = URLEncoder.encode(channelBody, StandardCharsets.UTF_8);
        String apiUrl = String.format("https://app-cdn.jjwxc.com/bookstore/getFullPageV1?channelBody=%s&channelMore=1", escapedChannelBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("User-Agent", Config.USER_AGENT)
                .GET()
                .build();

        main.LOGGER.info("正在请求今日限免书籍列表～");
        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        main.LOGGER.debug("限免列表API响应体: {}", response.body());

        if (response.statusCode() == 200) {
            JSONObject rootJson = new JSONObject(response.body());
            return new BookListRoot(rootJson);
        } else {
            main.LOGGER.error("获取书籍列表失败QAQ，HTTP状态码: {}", response.statusCode());
            throw new IOException("请求失败QAQ，HTTP状态码: " + response.statusCode());
        }
    }
}


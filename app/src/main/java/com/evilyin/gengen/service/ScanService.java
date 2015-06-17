package com.evilyin.gengen.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.evilyin.gengen.AccessTokenKeeper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Timer;
import java.util.TimerTask;

import cn.byr.bbs.sdk.api.ArticleApi;
import cn.byr.bbs.sdk.api.SearchApi;
import cn.byr.bbs.sdk.api.SectionApi;
import cn.byr.bbs.sdk.auth.Oauth2AccessToken;
import cn.byr.bbs.sdk.exception.BBSException;
import cn.byr.bbs.sdk.net.RequestListener;

/**
 * 搜索发帖
 *
 * @author evilyin(ChenZhixi)
 * @since 2015-6-3
 */

public class ScanService extends Service {

    private static final String doubanUrl = "http://www.douban.com/group/search?cat=1013&q=";
    private static final String baiduUrl = "http://www.baidu.com/s?wd=";
    private static final int scanTime = 600;//搜索间隔时长（秒）

    private Oauth2AccessToken mAccessToken;
    private Timer timer;
    private String resultUrl = "";


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.i("ScanService", "启动");

        mAccessToken = AccessTokenKeeper.readAccessToken(this);
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    SectionApi mSectionApi = new SectionApi(mAccessToken);
                    mSectionApi.getSection("4", new sectionListener());
                    mSectionApi.getSection("5", new sectionListener());
                    mSectionApi.getSection("6", new sectionListener());
                }
                super.handleMessage(msg);
            }
        };
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        };
        timer.schedule(task, 1000, scanTime * 1000);
    }

    @Override
    public void onDestroy() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        Log.i("ScanService", "搜索停止");
        super.onDestroy();
    }

    /**
     * 通过搜索引擎搜索关键字
     *
     * @param keyword 关键字
     * @return 搜索排行第一的网址url，仅当关键字包含或包含于所搜结果时有效，否则为空
     */
    private String search(String keyword) {
        String result = "";
        if (keyword.length() >= 23) {
            keyword = keyword.substring(0, 22);
        }
        try {
            Document document = Jsoup.connect(doubanUrl + URLEncoder.encode(keyword, "UTF-8")).get();
            Elements links = document.select("td.td-subject > a[href]");
            Element link = links.get(0);
            String linkKeyword = link.text();
            if (keyword.contains(linkKeyword) || linkKeyword.contains(keyword)) {
                result = link.attr("href");
//            } else {
//                document = Jsoup.connect(baiduUrl + URLEncoder.encode(keyword + " site:www.douban.com","UTF-8")).get();
//                links = document.select("h3.t > a[href]");
//                link = links.get(0);
//                linkKeyword = link.text();
//                if (keyword.contains(linkKeyword) || linkKeyword.contains(keyword)) {
//                    result = link.attr("href");
//                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i("search", "关键字搜索结果：" + result);
        return result;
    }

    class sectionListener implements RequestListener {

        @Override
        public void onComplete(String response) {
            //获取分区信息成功，开始搜索发帖
            try {
                JSONObject sectionObject = new JSONObject(response);
                JSONArray boardArray = sectionObject.getJSONArray("board");
                SearchApi mSearchApi = new SearchApi(mAccessToken);

                for (int i = 0; i < boardArray.length(); i++) {
                    String boardName = boardArray.getJSONObject(i).getString("name");
                    mSearchApi.threadByAuthor(boardName, "guitarmega", new searchListener());//根据用户名搜索
                    Thread.sleep(5000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onException(BBSException e) {
            e.printStackTrace();
        }
    }

    class searchListener implements RequestListener {

        @Override
        public void onComplete(String response) {
            //搜索结果取最新发帖
            try {
                JSONObject searchObject = new JSONObject(response);
                //判断搜索结果是否为空
                JSONObject pagination = searchObject.getJSONObject("pagination");
                if (pagination.getInt("item_all_count") != 0) {
                    //如果不为空，取最新的一帖
                    JSONArray searchArray = searchObject.getJSONArray("threads");
                    JSONObject resultThread = searchArray.getJSONObject(0);
                    final String title = resultThread.getString("title");
                    final String board = resultThread.getString("board_name");
                    final int id = resultThread.getInt("id");
                    int now = (int) (System.currentTimeMillis() / 1000);
                    if (now - resultThread.getInt("post_time") < 24 * 3600 * 7) {
                        //发帖时间小于搜索间隔，找到新帖
                        Log.i("ScanService", "找到新帖！标题：" + title + " 版面：" + board);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                resultUrl = search(title);
                                String resultContent = "";
                                if (!resultUrl.equals("")) {
                                    resultContent = "原帖地址：[url=" + resultUrl + "]" + resultUrl + "[/url]";
                                }
                                //发表回复
                                String content = "楼主sb\n" + resultContent;
                                ArticleApi mArticleApi = new ArticleApi(mAccessToken);
                                mArticleApi.reply(board, new articleListener(), "Re: " + title, content, id);
                            }
                        }).start();

                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onException(BBSException e) {
            e.printStackTrace();
        }
    }

    class articleListener implements RequestListener {

        @Override
        public void onComplete(String s) {
            Log.i("ScanService", "发帖成功");
        }

        @Override
        public void onException(BBSException e) {
            e.printStackTrace();
        }
    }
}

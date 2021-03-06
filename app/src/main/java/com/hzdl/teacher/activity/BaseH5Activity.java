package com.hzdl.teacher.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.hzdl.mex.socket.teacher.TeacherClient;
import com.hzdl.mex.utils.Log;
import com.hzdl.teacher.R;
import com.hzdl.teacher.base.BaseMidiActivity;
import com.hzdl.teacher.core.ActionBean;
import com.hzdl.teacher.core.ActionProtocol;
import com.hzdl.teacher.core.ActionResolver;

public class BaseH5Activity extends BaseMidiActivity {

    public static final String URL_ROOT = "http://teacher.huangzhongdalv.com/";
    private String questionIndex = "";

    WebView mWebview;
    WebSettings mWebSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    //销毁Webview
    @Override
    protected void onDestroy() {
        if (mWebview != null) {
            mWebview.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            mWebview.clearHistory();

            ((ViewGroup) mWebview.getParent()).removeView(mWebview);
            mWebview.destroy();
            mWebview = null;
        }
        super.onDestroy();
    }

    public void initH5() {

        mWebview = findViewById(R.id.webView);
        mWebSettings = mWebview.getSettings();
        mWebSettings.setJavaScriptEnabled(true);
        mWebSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        mWebview.addJavascriptInterface(new AndroidtoJs(), "android");

        //mWebview.loadUrl(URL_ROOT + "questionForTeacher.html");
        //mWebview.loadUrl("http://q.w3cstudy.cc/t/questionForTeacher.html");

        //设置WebChromeClient类
        mWebview.setWebChromeClient(new WebChromeClient() {
            //获取网站标题
            @Override
            public void onReceivedTitle(WebView view, String title) {
            }
            //获取加载进度
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress < 100) {
                    String progress = newProgress + "%";
                } else if (newProgress == 100) {
                    String progress = newProgress + "%";
                }
            }

        });

        mWebview.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mWebview.loadUrl("javascript:loadQuestion('" + name +"')");
            }
        });

    }

    String name;
    public void loadH5(final String vName){
        name = vName;
        mWebview.loadUrl(URL_ROOT);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                mWebview.loadUrl("javascript:loadQuestion('" + vName +"')");
//            }
//        },1000);
    }

    private ActionBean ab;
    /**
     * 收到学生端成绩消息
     * @param msg
     */
    @Override
    protected void handleMsg(Message action) {
        doAction((String) action.obj);
    }
    private void doAction(String str) {
        Log.e("kaka", "----------H5Activity code------- " + str);
        ab = ActionResolver.getInstance().resolve(str);
        if (ab.getCodeByPositon(0) == 2) {

            if(ab.getCodeByPositon(1) == 2){
                //教师端收到成绩  发送给h5
                mWebview.loadUrl("javascript:addStuScore('" + ab.getStringByPositon(2) + "')");
            }

        }
    }

    //---非公共逻辑-----------------------------------------------------------------------------------------------------

    // 继承自Object类  提供JS调用
    public class AndroidtoJs extends Object {

        // 定义JS需要调用的方法
        // 被JS调用的方法必须加入@JavascriptInterface注解
        @JavascriptInterface
        public void nextQuestion() {
            //下一步（题目）
            onNextQuestion();
        }

        @JavascriptInterface
        public void nextCourse(){
            //下一课
            onNextCourse();
        }

    }

    //教师端控制下一题
    public void onNextQuestion(){
        //sendSynAction(ActionProtocol.ACTION_TEST_NUM + "|" + "1");
        TeacherClient.getInstance().sendMsgToAll(ActionProtocol.ACTION_TEST_NUM + "|" + "1");
    }

    //点击返回上一页面而不是退出浏览器
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            sendSynAction(ActionProtocol.ACTION_TEST_OFF);
        }else if(keyCode == KeyEvent.KEYCODE_MENU ){
            mWebview.loadUrl("javascript:getQuestionList()");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //----子类覆盖-----------------------------------
    //下一节课
    public void onNextCourse(){
    }
    //获取id
    public String getID(){
        return "";
    }

}

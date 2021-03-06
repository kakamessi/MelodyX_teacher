package com.hzdl.teacher.downloadcourse;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.hzdl.mex.utils.SPUtils;
import com.hzdl.teacher.R;
import com.hzdl.teacher.activity.MainActivity;
import com.hzdl.teacher.base.BaseActivity;
import com.hzdl.teacher.base.Constant;
import com.hzdl.teacher.bean.CourseBean;
import com.hzdl.teacher.bean.CourseItemInfo;
import com.hzdl.teacher.bean.NewCourseInfo;
import com.hzdl.teacher.bean.PathBean;
import com.hzdl.teacher.downloadcourse.okhttp.HttpInfo;
import com.hzdl.teacher.downloadcourse.okhttp.OkHttpUtil;
import com.hzdl.teacher.downloadcourse.okhttp.OkHttpUtilInterface;
import com.hzdl.teacher.downloadcourse.okhttp.callback.CallbackOk;
import com.hzdl.teacher.downloadcourse.okhttp.util.GsonUtil;
import com.hzdl.teacher.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;
import static com.hzdl.teacher.downloadcourse.okhttp.annotation.CacheLevel.FIRST_LEVEL;

/**
 * 课程下载界面
 */
public class DownloadActivity extends BaseActivity {


    //--------------------------------------------
    public ListView lvCourse;
    private Button ib_back;
    private DownloadNewAdapter adapter;
    //下载标识  1-显示全部下载，2,3
    private int downLoadType = -1;

    private List<CourseItemInfo> ccourseList = new ArrayList<CourseItemInfo>();
    private NewCourseInfo nci;//网络下载封装成的课程信息总类

    private Button btn_dload_all;
    private View headView;

    private Handler myHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what){

                case 1:
                    //更新adapter
                    adapter.setData(ccourseList);
                    initHeadView();

                    break;

                case 2:

                    //删除全部数据完成
                    hideLoadingDialog();
                    adapter.setEmptyData();
                    setHeadViewType(1);

                    break;

                case 3:

                    //刷新
                    adapter.refreshProgress();
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_download);
        initView();
        initCourse();
    }

    @Override
    protected void handleMsg(Message msg) {

    }

    /**
     *
     * 1 下载， 2暂停 3删除
     */
    private void initView() {

        lvCourse = (ListView) findViewById(R.id.lv_course);
        lvCourse.setItemsCanFocus(true);
        lvCourse.setFocusable(false);
        ib_back = (Button) findViewById(R.id.ib_back);
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DownloadActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        adapter = new DownloadNewAdapter(this);

        btn_dload_all = (Button)findViewById(R.id.btn_dload_all);
        btn_dload_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(downLoadType==1){
                    adapter.downloadAll(true);
                    setHeadViewType(2);
                }else if(downLoadType==2){
                    adapter.downloadAll(false);
                    setHeadViewType(1);
                }else if(downLoadType == 3){
                    showDeleteAll();
                }
            }
        });

//        headView模块
//
//        headView = LayoutInflater.from(this).inflate(R.layout.dload_first_item, null);
//        btn_dload_all = (Button) headView.findViewById(R.id.btn_dload_all);
//        btn_dload_all.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                if(downLoadType==1){
//                    adapter.downloadAll(true);
//                    setHeadViewType(2);
//
//                }else if(downLoadType==2){
//                    adapter.downloadAll(false);
//                    setHeadViewType(1);
//
//                }else if(downLoadType == 3){
//
//                    showDeleteAll();
//                }
//
//            }
//        });

        lvCourse.setAdapter(adapter);
        adapter.bindAty(this);

        Utils.setOnFocusBG(ib_back, R.drawable.shape_strock, -1);
        Utils.setOnFocusBG(btn_dload_all, R.drawable.shape_strock, -1);
    }

    public void initHeadView(){
        float downNum = 0;
        float allNum = 0;
        for(CourseItemInfo cif : ccourseList){
            downNum = downNum + cif.getDone_num();
            allNum = allNum + cif.getAll_num();
        }
        if(downNum < allNum){
            setHeadViewType(1);
        }else if(downNum == allNum){
            setHeadViewType(3);

        }

    }

    public void refreashAdapter(){

        Message msg = Message.obtain();
        msg.what =3;
        myHandler.sendMessage(msg);

    }

    /**
     * 确定删除全部弹窗
     */
    private void showDeleteAll() {

                /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        final AlertDialog.Builder normalDialog = new AlertDialog.Builder(DownloadActivity.this);
        normalDialog.setTitle("提醒");
        normalDialog.setMessage("确认删除全部课程资源?");
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                        deleteAll();
                        showLoadingDialog();

                    }
                });
        normalDialog.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                    }
                });
        // 显示
        normalDialog.show();


    }

    private void deleteAll() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                File file = new File(Utils.getVideoPath());
                Utils.RecursionDeleteFile(file);

                Message msg = Message.obtain();
                msg.what =2;
                myHandler.sendMessage(msg);

            }
        }).start();

    }

    /**
     * 设置头View
     */
    public void setHeadViewType(int type){
        switch (type){
            case 1:
                btn_dload_all.setText("全部下载");
                downLoadType = 1;
                break;
            case 2:
                btn_dload_all.setText("全部暂停");
                downLoadType = 2;
                break;
            case 3:
                btn_dload_all.setText("全部删除");
                downLoadType = 3;
                break;
        }
    }

    /**
     *
     * 初始化课程信息
     *
     * 获取对应课程信息，与本地文件比对，初始化出已经下载信息
     *
     */
    private void initCourse() {

        String schoolId = (String) SPUtils.get(DownloadActivity.this,Constant.KEY_SCHOOL_ID,"");
        Toast.makeText(this,schoolId,0).show();

        String domainNameRequest = Constant.URL_ROOT;
        String courseInfoJson = Constant.DOWNLOAD_PATH;

        OkHttpUtilInterface okHttpUtil = OkHttpUtil.Builder()
                .setCacheLevel(FIRST_LEVEL)
                .setConnectTimeout(25).build(this);
        okHttpUtil.doGetAsync(
                HttpInfo.Builder().setUrl(domainNameRequest + courseInfoJson).addParam
                        ("sid", schoolId)//需要传入课程id参数
                        .build(),
                new CallbackOk() {
                    @Override
                    public void onResponse(HttpInfo info) throws IOException {

                        final String jsonResult = info.getRetDetail();
                        if (info.isSuccessful()) {

                            //lvCourse.addHeaderView(headView);

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    nci = GsonUtil.jsonToObject(jsonResult, NewCourseInfo.class);//Gson解析
                                    if (nci.getCode() == 200) {
                                        //封装数据
                                        List<CourseBean> lp = nci.getDetail();


                                        //for循环生成bean
                                        for(int i=0; i<lp.size(); i++){

                                            List<PathBean> lb = lp.get(i).getPathList();
                                            CourseItemInfo cii = new CourseItemInfo();

                                            int done_num = 0;

                                            for(PathBean pb : lb){
                                                String vName = pb.getVideoPath().substring(pb.getVideoPath().lastIndexOf("/")+1);
                                                cii.getResUrl().put(vName, Constant.URL_COURSE_DOWNLOAD + pb.getVideoPath());
                                            }

                                            //检测已下载文件数量
                                            for (Map.Entry<String, String> entry : cii.getResUrl().entrySet()) {
                                                //System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
                                                if(Utils.isFileExist(entry.getKey())){
                                                    done_num++;
                                                }
                                            }

                                            cii.setCourse_name(lp.get(i).getName());
                                            cii.setAll_num(cii.getResUrl().size());
                                            cii.setDone_num(done_num);

                                            //根据文件下载进度  生成状态
                                            if(cii.getDone_num()==cii.getAll_num()){
                                                cii.setIsActive(4);
                                            }else{
                                                cii.setIsActive(1);
                                            }

                                            ccourseList.add(cii);
                                        }

                                        Message msg = Message.obtain();
                                        msg.what =1;
                                        myHandler.sendMessage(msg);

                                    }
                                }
                            }).start();
                        }
                    }
                });
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK) {

            Intent intent = new Intent(DownloadActivity.this, MainActivity.class);
            startActivity(intent);

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

package com.hzdl.teacher.activity;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hzdl.teacher.R;
import com.hzdl.teacher.base.App;
import com.hzdl.teacher.base.BaseActivity;
import com.hzdl.teacher.base.Constant;
import com.hzdl.teacher.bean.lesson.LessonInfo;
import com.hzdl.teacher.bean.lesson.SimpleSection;
import com.hzdl.teacher.core.ActionBean;
import com.hzdl.teacher.core.ActionProtocol;
import com.hzdl.teacher.core.ActionResolver;
import com.hzdl.teacher.utils.Encrypter;
import com.hzdl.teacher.utils.Utils;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CourseChoseActivity extends BaseActivity {

    @BindView(R.id.rcy_course)
    RecyclerView rcy_course;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_chose);
        ButterKnife.bind(this);

        initRcy();
    }

    private void initRcy() {

        //设置布局管理器
        rcy_course.setLayoutManager(new GridLayoutManager(this, 5));
        CCAdapter cc = new CCAdapter();
        cc.setOnItemClickLitener(new OnItemClickLitener() {
            @Override
            public void onItemClick(View view, int position) {

                App.getApplication().setIndexLessonOn(position);
                //提前解密视频文件
                String vedioName = "|" + getVNames();
                sendSynAction(ActionProtocol.ACTION_COURSE_START + vedioName);
                CourseChoseActivity.this.finish();

            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
        rcy_course.setAdapter(cc);
        rcy_course.setItemAnimator(new DefaultItemAnimator());
        rcy_course.addItemDecoration(new SpaceItemDecoration(50));


    }

    private String getVNames() {

        LessonInfo les = mBaseApp.getLi().get(mBaseApp.getIndexLessonOn());
        StringBuffer sb = new StringBuffer();
        for(SimpleSection ss : les.getSectionsList()){
            if(Constant.SECTION_TYPE_VIDEO == ss.getType() || Constant.SECTION_TYPE_VIDEO_H5 == ss.getType()){
                sb.append(ss.getSourceName() + "_");
            }
        }

        return sb.toString();
    }

    @OnClick(R.id.iv_back)
    public void onClick() {
        finish();
    }

    public interface OnItemClickLitener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    class CCAdapter extends RecyclerView.Adapter<CCAdapter.MyViewHolder> {


        private OnItemClickLitener mOnItemClickLitener;

        public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener) {
            this.mOnItemClickLitener = mOnItemClickLitener;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(CourseChoseActivity.this).inflate(R.layout.item_c_c, parent, false);
            v.setFocusable(true);
            Utils.setOnFocusBG(v,R.drawable.shape_strock,-1);
            final MyViewHolder holder = new MyViewHolder(v);

            if (mOnItemClickLitener != null) {
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int pos = holder.getLayoutPosition();
                        mOnItemClickLitener.onItemClick(holder.itemView, pos);
                    }
                });
            }

            return holder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            holder.tv_name.setText(mBaseApp.getLi().get(position).getName());
        }

        @Override
        public int getItemCount() {
            return App.getApplication().getLi().size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView tv;
            TextView tv_name;

            public MyViewHolder(View view) {
                super(view);
                tv = (ImageView) view.findViewById(R.id.imageView);
                tv_name = (TextView) view.findViewById(R.id.tv_name);
            }
        }
    }

    class SpaceItemDecoration extends RecyclerView.ItemDecoration {

        int mSpace;

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);

            outRect.left = mSpace;
            outRect.right = mSpace;
            outRect.bottom = mSpace;
            outRect.top = 10;

        }

        public SpaceItemDecoration(int space) {
            this.mSpace = space;
        }
    }


    @Override
    protected void handleMsg(Message msg) {
        try {
            doAction((String) msg.obj);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private ActionBean ab;

    private void doAction(String str) {
        ab = ActionResolver.getInstance().resolve(str);

        int c2 = Integer.parseInt(ab.getCodes()[1]);
        int c3 = Integer.parseInt(ab.getCodes()[2]);
        String s4 = ab.getCodes()[3];

        if (c2 == ActionProtocol.CODE_ACTION_COURSE) {
            if (c3 == 1) {

                final String[] names = s4.split("_");

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            for(int i =0; i<names.length; i++){
                                File f1 = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Constant.FILE_PATH_CACHE + names[i]);
                                File f2 = new File(Utils.getVideoPath() + names[i]);
                                Encrypter.decode(f1,f2,"xmelody");
                            }
                        }catch (Exception e){
                        }
                    }
                }).start();

                Intent intent = new Intent(CourseChoseActivity.this, CourseActivity.class);
                startActivity(intent);

            }
        }
    }


}

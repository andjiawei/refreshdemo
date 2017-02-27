package com.example.jiawei.refreshdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import java.util.ArrayList;
import java.util.List;

import okhttp.CommonOkHttpClient;
import okhttp.listener.DisposeDataHandle;
import okhttp.listener.DisposeDataListener;
import okhttp.request.CommonRequest;
import okhttp.request.RequestParams;

public class MainActivity extends AppCompatActivity {

    private String url="https://api.douban.com/v2/movie/in_theaters";
    private String apikey="0b2bdeda43b5688921839c8ecb20399b";
    private XRecyclerView listView;
    private MyAdapter adapter;
    private Model model;
    private List<Model.SubjectsBean> subjects=new ArrayList<>();
    private String action;
    private static final String REFRESH="refresh";
    private int page;
    private int pageCount=10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (XRecyclerView) findViewById(R.id.listView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        listView.setLayoutManager(layoutManager);
        listView.setAdapter(adapter=new MyAdapter());
        loadData(REFRESH);
        listView.refresh();
        listView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                page=0;
                loadData(REFRESH);
            }

            @Override
            public void onLoadMore() {
                page=page+pageCount;
                loadData("loadmore");
            }
        });
    }

    private void loadData(final String action) {
        //apikey：固定值0b2bdeda43b5688921839c8ecb20399b
//        city：所在城市，例如北京、上海等
//        start：分页使用，表示第几页
//        count：分页使用，表示数量
        RequestParams params = new RequestParams();
        params.put("apikey",apikey);
        params.put("city","北京");
        params.put("start",page+"");
        params.put("count",pageCount+"");

        CommonOkHttpClient.get(CommonRequest.createGetRequest(url,params),new DisposeDataHandle(
                new DisposeDataListener() {

                    @Override
                    public void onSuccess(Object responseObj) {
                        Log.e("成功", "onSuccess: "+responseObj );
                        model=(Model) responseObj;
                        if(TextUtils.equals(action,REFRESH)){
                            subjects.clear();
                            subjects.addAll(model.getSubjects());
                            adapter.notifyDataSetChanged();
                            listView.refreshComplete();
                        }else{
                            subjects.addAll(model.getSubjects());
                            adapter.notifyDataSetChanged();
                            listView.loadMoreComplete();
                        }
                    }

                    @Override
                    public void onFailure(Object reasonObj) {
                        Log.e("失败", "onFailure: " +reasonObj);
                    }
                },Model.class));
    }

    class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>{


        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item,viewGroup,false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            holder.imageView.setImageURI(subjects.get(position).getCasts().get(0).getAvatars().getSmall());
            holder.textView.setText(subjects.get(position).getCasts().get(0).getName());
        }

        @Override
        public int getItemCount() {
            return subjects.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder{

            public ViewHolder(View v){
                super(v);
                imageView= (SimpleDraweeView) v.findViewById(R.id.my_image_view);
                textView=(TextView) v.findViewById(R.id.textView);
            }
            public SimpleDraweeView imageView;
            public TextView textView;

        }
     }

}

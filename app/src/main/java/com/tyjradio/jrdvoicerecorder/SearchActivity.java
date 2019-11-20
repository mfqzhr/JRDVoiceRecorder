package com.tyjradio.jrdvoicerecorder;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.tyjradio.jrdvoicerecorder.adapter.HistoryAdapter;
import com.tyjradio.jrdvoicerecorder.adapter.SearchAdapter;
import com.tyjradio.jrdvoicerecorder.bean.AudioRecorderItemBean;
import com.tyjradio.jrdvoicerecorder.db.SearchHistoryDBManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SearchActivity extends AppCompatActivity {
    @BindView(R.id.id_search_listview)
    ListView mListView;

    @BindView(R.id.id_et_name)
    EditText etName;

    @BindView(R.id.id_history_gv)
    GridView historyGv;

    @BindView(R.id.id_search_history_ll)
    LinearLayout llSearchHistory;

    private List<AudioRecorderItemBean> allAudios=new ArrayList<>();

    private List<AudioRecorderItemBean> searchAudios=new ArrayList<>();
    private SearchAdapter adapter;

    private HistoryAdapter historyAdapter;

    private static final int GET_SEARCH_HISTORY_SUCCESS = 44;
    private SearchHistoryDBManager searchHistoryDBManager;
    private List<String> searchHistorys = new ArrayList<>();
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_SEARCH_HISTORY_SUCCESS:
                    //设置历史搜索记录
                    setHistory();
                    break;
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);
        Intent intent=getIntent();
        Bundle bundle=intent.getBundleExtra("bundle");
        allAudios= (List<AudioRecorderItemBean>) bundle.getSerializable("all");
        adapter=new SearchAdapter(this,searchAudios);
        mListView.setAdapter(adapter);
        historyGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getSearch(searchHistorys.get(position));
            }
        });
        getSearchHistory();

    }


    public void setHistory(){
        llSearchHistory.setVisibility(View.VISIBLE);
        historyAdapter=new HistoryAdapter(this,searchHistorys);
        historyGv.setAdapter(historyAdapter);
    }

    public void getSearchHistory(){
        if (searchHistoryDBManager == null) {
            searchHistoryDBManager = new SearchHistoryDBManager(SearchActivity.this);
        }
        new Thread() {
            @Override
            public void run() {
                searchHistorys = searchHistoryDBManager.getAllSearchHistory();
                Message message = new Message();
                message.what = GET_SEARCH_HISTORY_SUCCESS;
                handler.sendMessage(message);
            }
        }.start();
    }


    /**
     * 添加历史记录
     */

    private void addHistory(final String text) {
        if (searchHistoryDBManager == null) {
            searchHistoryDBManager = new SearchHistoryDBManager(SearchActivity.this);
        }
        new Thread() {
            @Override
            public void run() {
                searchHistoryDBManager.insertSearchHistoryData(text);
            }
        }.start();
    }



    public void getSearch(String name){
        //searchAudios=AudioUtils.getName(this,name);
        searchAudios.clear();
        for(int i=0;i<allAudios.size();i++){
            Log.d("查询", "getSearch: " + allAudios.get(i).getAudioFile() + name + " " + allAudios.get(i).getAudioFile().equalsIgnoreCase(name));
            if(allAudios.get(i).getAudioFile().toLowerCase().contains(name.toLowerCase())){
                searchAudios.add(allAudios.get(i));
            }
        }
        llSearchHistory.setVisibility(View.GONE);
        adapter.refreshData(searchAudios);
    }

    @OnClick(R.id.id_search_tv)
    void search(){
        String name=etName.getText().toString();
        if(TextUtils.isEmpty(name)){
            Toast.makeText(SearchActivity.this,"搜索内容不能为空",Toast.LENGTH_SHORT).show();
            return;
        }else {
            addHistory(name);
            getSearch(name);
        }
    }

    @OnClick(R.id.id_back_iv)
    void closeActivity(){
        this.finish();
    }


}

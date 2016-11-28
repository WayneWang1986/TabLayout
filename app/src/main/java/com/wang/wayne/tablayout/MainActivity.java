package com.wang.wayne.tablayout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.wang.wayne.library.TabLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tab_layout)
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        List<String> mockData = new ArrayList<>();
        mockData.add("今天");
        mockData.add("大后天");
        mockData.add("我们");
        mockData.add("打羽毛球");

        tabLayout.setData(mockData);
        tabLayout.setOnTabClickListener(new TabLayout.OnTabClickListener() {
            @Override
            public void onTabClick(int index) {
                Log.i("tabIndex", " ===== index = " + index);
            }
        });
    }
}

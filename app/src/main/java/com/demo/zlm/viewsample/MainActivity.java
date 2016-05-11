package com.demo.zlm.viewsample;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView= (TextView) findViewById(R.id.textView);
    }
    public void doClick(View v){
        textView.layout(0,0,200,200);
        System.out.println(textView.getHeight()+"--"+textView.getWidth());
        System.out.println(textView.getMeasuredHeight()+"--"+textView.getMeasuredWidth());
    }

}

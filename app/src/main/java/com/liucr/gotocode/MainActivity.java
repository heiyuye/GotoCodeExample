package com.liucr.gotocode;

import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends BaseActivity {

//    @Override
//    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
//        super.onCreate(savedInstanceState, persistentState);
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//    }

    @Override
    int initLayout() {
        return R.layout.activity_main;
    }

    public void onClickTextView(View view) {
        method1();
        method3();
        Toast.makeText(this, "onClickTextView", Toast.LENGTH_LONG).show();
    }

    public void method1() {
        method2();
        Toast.makeText(this, "method1", Toast.LENGTH_LONG).show();
    }

    public void method2() {
        Toast.makeText(this, "method2", Toast.LENGTH_LONG).show();
    }

    public void method3() {
        Toast.makeText(this, "method3", Toast.LENGTH_LONG).show();
    }
}

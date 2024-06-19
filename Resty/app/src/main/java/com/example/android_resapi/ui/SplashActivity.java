package com.example.android_resapi.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Handler;

import com.example.android_resapi.R;

// SplashActivity 클래스는 앱이 실행될 때 처음으로 표시되는 스플래시 화면을 처리하는 액티비티입니다.
public class SplashActivity extends AppCompatActivity {

    // onCreate 메서드는 액티비티가 생성될 때 호출됩니다.
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); // 스플래시 화면 레이아웃 설정

        // 액션 바 숨기기
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        // Handler를 사용하여 3초 후에 MainActivity로 전환
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish(); // 현재 액티비티 종료
            }
        }, 3000); // 3초 있다 메인액티비티로 전환
    }

    // onPause 메서드는 액티비티가 일시 정지 상태로 전환될 때 호출됩니다.
    protected void onPause() {
        super.onPause();
        finish(); // 현재 액티비티 종료
    }
}

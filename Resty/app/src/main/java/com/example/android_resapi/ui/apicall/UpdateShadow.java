package com.example.android_resapi.ui.apicall;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;
import java.net.MalformedURLException;
import java.net.URL;

import com.example.android_resapi.httpconnection.PutRequest;

// UpdateShadow 클래스는 서버에 PUT 요청을 보내기 위해 PutRequest 클래스를 상속받아 구현된 클래스입니다.
public class UpdateShadow extends PutRequest {
    final static String TAG = "AndroidAPITest"; // 로그 태그
    String urlStr; // 요청할 URL 문자열

    // UpdateShadow 생성자, Activity와 URL 문자열을 인자로 받습니다.
    public UpdateShadow(Activity activity, String urlStr) {
        super(activity); // 상위 클래스 생성자 호출
        this.urlStr = urlStr; // URL 문자열 초기화
    }

    // 요청 전에 URL을 설정합니다.
    @Override
    protected void onPreExecute() {
        try {
            Log.e(TAG, urlStr); // URL 문자열을 로그로 출력
            url = new URL(urlStr); // URL 객체를 생성합니다.

        } catch (MalformedURLException e) {
            // URL이 잘못된 경우 예외 처리
            e.printStackTrace();
            Toast.makeText(activity, "URL is invalid:" + urlStr, Toast.LENGTH_SHORT).show();
            activity.finish(); // 액티비티를 종료합니다.
        }
    }

    // 요청 후 결과를 처리합니다.
    @Override
    protected void onPostExecute(String result) {
        Toast.makeText(activity, result, Toast.LENGTH_SHORT).show(); // 결과를 토스트 메시지로 출력합니다.
    }
}

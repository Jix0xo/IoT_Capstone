package com.example.android_resapi.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.LineData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.example.android_resapi.R;
import com.example.android_resapi.ui.apicall.GetLog;

public class LogActivity extends AppCompatActivity {
    // 서버에서 로그를 가져올 URL을 저장할 문자열 변수
    String getLogsURL;
    // 시작 날짜와 종료 날짜를 표시할 텍스트뷰
    private TextView textView_Date1;
    private TextView textView_Date2;
    // DatePickerDialog의 날짜 선택 콜백 메서드를 저장할 변수
    private DatePickerDialog.OnDateSetListener callbackMethod;
    // 로그를 위한 태그 상수
    final static String TAG = "AndroidAPITest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 이 액티비티의 레이아웃 설정
        setContentView(R.layout.activity_device_log);

        // Intent에서 로그 URL을 가져옴
        Intent intent = getIntent();
        getLogsURL = intent.getStringExtra("getLogsURL");
        Log.i(TAG, "getLogsURL=" + getLogsURL);

        // 시작 날짜 버튼을 찾고 클릭 리스너 설정
        Button startDateBtn = findViewById(R.id.start_date_button);
        startDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // DatePickerDialog의 날짜 선택 콜백 메서드 설정
                callbackMethod = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // 선택한 날짜를 시작 날짜 텍스트뷰에 설정
                        textView_Date1 = findViewById(R.id.textView_date1);
                        textView_Date1.setText(String.format("%d-%02d-%02d ", year, monthOfYear + 1, dayOfMonth));
                    }
                };

                // DatePickerDialog 생성 및 표시
                DatePickerDialog dialog = new DatePickerDialog(LogActivity.this, callbackMethod, 2020, 12, 0);
                dialog.show();
            }
        });

        // 종료 날짜 버튼을 찾고 클릭 리스너 설정
        Button endDateBtn = findViewById(R.id.end_date_button);
        endDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // DatePickerDialog의 날짜 선택 콜백 메서드 설정
                callbackMethod = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // 선택한 날짜를 종료 날짜 텍스트뷰에 설정
                        textView_Date2 = findViewById(R.id.textView_date2);
                        textView_Date2.setText(String.format("%d-%02d-%02d ", year, monthOfYear + 1, dayOfMonth));
                    }
                };

                // DatePickerDialog 생성 및 표시
                DatePickerDialog dialog = new DatePickerDialog(LogActivity.this, callbackMethod, 2020, 12, 0);
                dialog.show();
            }
        });

        // 로그 시작 버튼을 찾고 클릭 리스너 설정
        Button start = findViewById(R.id.log_start_button);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // GetLog AsyncTask 실행하여 로그 데이터를 가져옴
                new GetLog(LogActivity.this, getLogsURL).execute();
            }
        });
    }
}

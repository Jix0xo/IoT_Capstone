package com.example.android_resapi.ui;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.example.android_resapi.R;
import com.example.android_resapi.ui.apicall.UpdateShadow;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DeviceActivity extends AppCompatActivity {
    // URL과 자세를 저장할 문자열 변수 선언
    String urlStr, posture;
    // 로깅을 위한 태그 상수 선언
    final static String TAG = "AndroidAPITest";
    RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 이 액티비티의 레이아웃 설정
        setContentView(R.layout.activity_device);

        // 이 액티비티를 시작한 Intent에서 URL을 가져옴
        Intent intent = getIntent();
        urlStr = intent.getStringExtra("thingShadowURL");

        // 시계 이미지뷰를 찾고 클릭 리스너 설정
        ImageView clock = findViewById(R.id.clock);
        clock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 첫 번째 TimePickerDialog 리스너 정의
                TimePickerDialog.OnTimeSetListener listener_start = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // 선택한 시간을 텍스트뷰에 설정
                        TextView textView_Time1 = findViewById(R.id.textView_time1);
                        textView_Time1.setText(String.format("%02d:%02d", hourOfDay, minute));
                        Log.i(TAG, "시작 시간 선택:" + textView_Time1.getText().toString());
                        // 첫 번째 TimePickerDialog의 확인 버튼을 누른 후에 두 번째 TimePickerDialog를 보여줌
                        showSecondTimePickerDialog();
                    }
                };

                // 첫 번째 TimePickerDialog 생성 및 표시
                TimePickerDialog dialog_start = new TimePickerDialog(DeviceActivity.this, listener_start, 0, 0, false);
                dialog_start.show();
            }

            // 두 번째 TimePickerDialog를 표시하는 메서드
            private void showSecondTimePickerDialog() {
                // 두 번째 TimePickerDialog 리스너 정의
                TimePickerDialog.OnTimeSetListener listener_end = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // 선택한 시간을 텍스트뷰에 설정
                        TextView textView_Time2 = findViewById(R.id.textView_time2);
                        textView_Time2.setText(String.format("%02d:%02d", hourOfDay, minute));
                        Log.i(TAG, "끝나는 시간 선택:" + textView_Time2.getText().toString());
                    }
                };

                // 두 번째 TimePickerDialog 생성 및 표시
                TimePickerDialog dialog_end = new TimePickerDialog(DeviceActivity.this, listener_end, 0, 0, false);
                dialog_end.show();
            }
        });

        // 업데이트 버튼을 찾고 클릭 리스너 설정
        Button updateBtn = findViewById(R.id.updateBtn);
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Shadow 업데이트 메서드 호출
                updateShadow();
            }
        });

        // 라디오 그룹을 찾고 체크 변경 리스너 설정
        radioGroup = findViewById(R.id.radio_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // 체크된 라디오 버튼에 따라 자세 설정 및 로그 출력
                switch (checkedId) {
                    case R.id.posture_1:
                        posture = "1";
                        Log.i(TAG, "posture 선택: " + posture);
                        Toast.makeText(DeviceActivity.this, "정자세 선택", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.posture_2:
                        posture = "2";
                        Log.i(TAG, "posture 선택: " + posture);
                        Toast.makeText(DeviceActivity.this, "측면 선택", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.posture_3:
                        posture = "3";
                        Log.i(TAG, "posture 선택: " + posture);
                        Toast.makeText(DeviceActivity.this, "코골이 완화 선택", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }

    // Shadow 업데이트 메서드
    private void updateShadow() {
        // 시작 시간과 끝나는 시간을 텍스트뷰에서 가져옴
        TextView textView_Time1 = findViewById(R.id.textView_time1);
        String startTime = textView_Time1.getText().toString();
        TextView textView_Time2 = findViewById(R.id.textView_time2);
        String endTime = textView_Time2.getText().toString();

        // JSON 객체 생성
        JSONObject payload = new JSONObject();

        try {
            // JSON 배열 생성
            JSONArray jsonArray = new JSONArray();

            // 자세가 설정되어 있으면 JSON 객체에 추가
            if (posture != null && !posture.isEmpty()) {
                JSONObject tag1 = new JSONObject();
                tag1.put("tagName", "posture");
                tag1.put("tagValue", posture);
                jsonArray.put(tag1);
            }

            // 시작 시간이 설정되어 있으면 JSON 객체에 추가
            if (startTime != null && !startTime.isEmpty()) {
                JSONObject tag2 = new JSONObject();
                tag2.put("tagName", "startTime");
                tag2.put("tagValue", startTime);
                jsonArray.put(tag2);
            }

            // 끝나는 시간이 설정되어 있으면 JSON 객체에 추가
            if (endTime != null && !endTime.isEmpty()) {
                JSONObject tag3 = new JSONObject();
                tag3.put("tagName", "endTime");
                tag3.put("tagValue", endTime);
                jsonArray.put(tag3);
            }

            // JSON 배열을 JSON 객체에 추가
            if (jsonArray.length() > 0) {
                payload.put("tags", jsonArray);
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSONException");
        }

        // JSON 객체 로그 출력
        Log.i(TAG, "payload=" + payload);
        // JSON 객체가 비어 있지 않으면 업데이트 작업 실행
        if (payload.length() > 0) {
            new UpdateShadow(DeviceActivity.this, urlStr).execute(payload);
        } else {
            Toast.makeText(DeviceActivity.this, "업데이트 할 상태 정보 입력이 필요합니다", Toast.LENGTH_SHORT).show();
        }
    }
}

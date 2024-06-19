package com.example.android_resapi.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.android_resapi.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

// MainActivity 클래스는 앱의 주요 활동을 처리하는 액티비티입니다.
public class MainActivity extends AppCompatActivity {
    final static String TAG = "AndroidAPITest"; // 로그 태그
    EditText saveLink; // 링크 입력을 위한 EditText
    Spinner linkSpinner; // 저장된 링크를 선택하기 위한 Spinner
    List<String> linkList; // 링크 목록을 저장하는 List
    ArrayAdapter<String> linkAdapter; // 링크 목록을 관리하는 ArrayAdapter

    private String newLink; // 새로 선택된 링크를 저장하는 변수

    private TextView currentLinkTextView; // 현재 선택된 링크를 표시하는 TextView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        saveLink = findViewById(R.id.saveLink); // 링크 입력 EditText 초기화
        linkSpinner = findViewById(R.id.linkSpinner); // 링크 선택 Spinner 초기화

        linkList = new ArrayList<>(); // 링크 목록 초기화
        linkAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, linkList); // 링크 어댑터 초기화
        linkAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // 드롭다운 뷰 리소스 설정
        linkSpinner.setAdapter(linkAdapter); // 스피너에 어댑터 설정

        // 링크 목록을 SharedPreferences에서 읽어옴
        SharedPreferences sharedPreferences = getSharedPreferences("dropdown_data", MODE_PRIVATE);
        Set<String> linkSet = sharedPreferences.getStringSet("links", new HashSet<String>());
        Log.d(TAG, "현재 SharedPreferences에 저장된 링크 목록: " + linkSet.toString());

        linkList.addAll(linkSet); // 링크 목록에 추가
        linkAdapter.notifyDataSetChanged(); // 변경된 데이터를 어댑터에 알림

        Button addLinkBtn = findViewById(R.id.addLinkBtn); // 링크 추가 버튼 초기화
        addLinkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // EditText에서 링크를 가져옴
                String newLink = saveLink.getText().toString().trim();
                // 링크가 비어있지 않으면 추가
                if (!newLink.isEmpty()) {
                    // 중복 체크
                    if (!linkList.contains(newLink)) {
                        // 링크 추가
                        linkList.add(newLink);
                        // SharedPreferences에 링크 목록 저장
                        SharedPreferences sharedPreferences = getSharedPreferences("dropdown_data", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        Set<String> linkSet = new HashSet<>(linkList);
                        editor.putStringSet("links", linkSet);
                        editor.apply();
                        // 어댑터 갱신
                        linkAdapter.notifyDataSetChanged();
                        // Toast 메시지 표시
                        Toast.makeText(MainActivity.this, "링크가 추가되었습니다.", Toast.LENGTH_SHORT).show();
                        // 로그 추가
                        Log.d(TAG, "링크가 추가되었습니다: " + newLink);
                    } else {
                        // 중복된 링크가 있는 경우 Toast 메시지 표시
                        Toast.makeText(MainActivity.this, "이미 추가된 링크입니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // 링크가 비어있는 경우 Toast 메시지 표시
                    Toast.makeText(MainActivity.this, "링크를 입력하세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button deleteLinkBtn = findViewById(R.id.deleteLinkBtn); // 링크 삭제 버튼 초기화
        deleteLinkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Spinner에서 선택된 링크 가져오기
                String selectedLink = linkSpinner.getSelectedItem().toString();

                // 선택된 링크 삭제
                linkList.remove(selectedLink);

                // 링크 어댑터 갱신
                linkAdapter.notifyDataSetChanged();

                // SharedPreferences를 사용하여 링크 목록 저장
                SharedPreferences sharedPreferences = getSharedPreferences("dropdown_data", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                Set<String> updatedLinkSet = new HashSet<>(linkList);
                editor.putStringSet("links", updatedLinkSet);
                editor.apply();
            }
        });

        Button chooseLinkBtn = findViewById(R.id.makeLinkBtn); // 링크 선택 버튼 초기화
        chooseLinkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 선택된 부분 가져오기
                String selectedPart = linkSpinner.getSelectedItem() != null ? linkSpinner.getSelectedItem().toString() : "";

                if (selectedPart.isEmpty()) {
                    Toast.makeText(MainActivity.this, "베개를 선택해주세요", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "Device: 선택되지 않음");
                } else {
                    Toast.makeText(MainActivity.this, "선택 완료", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "Device: " + selectedPart);

                    // 고정된 값
                    String fixedPart = "https://**********.execute-api.ap-northeast-2.amazonaws.com/prod/devices/";
                    // 새로운 링크 구성
                    String newLink = fixedPart + selectedPart;

                    currentLinkTextView = findViewById(R.id.currentLinkTextView);
                    currentLinkTextView.setText("현재 선택된 베개 : " + selectedPart);

                    MainActivity.this.newLink = newLink;
                }
            }
        });

        Button thingShadowBtn = findViewById(R.id.thingShadowBtn); // 사물 상태 조회/변경 버튼 초기화
        thingShadowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (newLink == null || newLink.equals("")) {
                    Toast.makeText(MainActivity.this, "사물상태 조회/변경 API URI 입력이 필요합니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.d(TAG, "접속 링크: " + newLink);
                Intent intent = new Intent(MainActivity.this, DeviceActivity.class);
                intent.putExtra("thingShadowURL", newLink);
                startActivity(intent);
            }
        });

        Button listLogsBtn = findViewById(R.id.listLogsBtn); // 로그 조회 버튼 초기화
        listLogsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String urlstr = newLink +"/log"; // 로그 조회 API 링크 생성
                if (urlstr == null || urlstr.equals("")) {
                    Toast.makeText(MainActivity.this, "사물로그 조회 API URI 입력이 필요합니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(MainActivity.this, LogActivity.class);
                intent.putExtra("getLogsURL", urlstr);
                startActivity(intent);
            }
        });
    }
}

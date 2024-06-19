package com.example.android_resapi.ui.apicall;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android_resapi.R;
import com.example.android_resapi.httpconnection.GetRequest;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GetLog extends GetRequest {
    final static String TAG = "AndroidAPITest";
    String urlStr;

    public GetLog(Activity activity, String urlStr) {
        super(activity);
        this.urlStr = urlStr;
    }

    @Override
    protected void onPreExecute() {
        try {
            // 날짜 범위를 얻기 위해 텍스트뷰 참조
            TextView textView_Date1 = activity.findViewById(R.id.textView_date1);
            TextView textView_Date2 = activity.findViewById(R.id.textView_date2);

            // API 호출을 위한 파라미터 설정
            String params = String.format("?from=%s:00&to=%s:00", textView_Date1.getText().toString() + "00:00",
                    textView_Date2.getText().toString() + "23:49");

            Log.i(TAG, "urlStr=" + urlStr + params);
            url = new URL(urlStr + params);

        } catch (MalformedURLException e) {
            Toast.makeText(activity, "URL이 잘못되었습니다:" + urlStr, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        TextView message_s = activity.findViewById(R.id.message2);
        message_s.setText("조회 중...");
    }

    @Override
    protected void onPostExecute(String jsonString) {
        TextView message_s = activity.findViewById(R.id.message2);
        TextView message_m = activity.findViewById(R.id.message3);

        // 결과가 없는 경우 처리
        if (jsonString == null) {
            message_s.setText("로그 없음");
            message_m.setText("로그 없음");
            return;
        }
        message_s.setText("코골이 그래프");
        message_m.setText("뒤척임 그래프");

        // JSON 문자열을 파싱하여 태그 목록 생성
        ArrayList<Tag> arrayList = getArrayListFromJSONString(jsonString);

        // 시간대별 최대 값을 집계하기 위한 데이터 준비
        Map<String, Integer> maxSnoringByHour = new HashMap<>();
        Map<String, Integer> maxMovingByHour = new HashMap<>();

        // x축 라벨 리스트 생성
        Map<String, String> xLabelsMap_s = new HashMap<>();
        Map<String, String> xLabelsMap_m = new HashMap<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (Tag tag : arrayList) {
            try {
                Date date = sdf.parse(tag.timestamp);
                String hour = new SimpleDateFormat("MM-dd HH").format(date);

                // snoring과 moving 값을 로그에 출력
                Log.i(TAG, "hour: " + hour + ", snoring: " + tag.snoring + ", moving: " + tag.moving);

                // snoring 값
                int snoringValue = Integer.parseInt(tag.snoring);
                int currentMaxSnoring = maxSnoringByHour.containsKey(hour) ? maxSnoringByHour.get(hour) : 0;
                maxSnoringByHour.put(hour, Math.max(currentMaxSnoring, snoringValue));

                // moving 값
                int movingValue = Integer.parseInt(tag.moving);
                int currentMaxMoving = maxMovingByHour.containsKey(hour) ? maxMovingByHour.get(hour) : 0;
                maxMovingByHour.put(hour, Math.max(currentMaxMoving, movingValue));

                // x축 라벨 저장
                xLabelsMap_s.put(hour, hour);
                xLabelsMap_m.put(hour, hour);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        // 시간대별 증가량을 계산
        ArrayList<BarEntry> snoringEntries = new ArrayList<>();
        ArrayList<BarEntry> movingEntries = new ArrayList<>();

        ArrayList<String> xLabels_s = new ArrayList<>(xLabelsMap_s.keySet());
        ArrayList<String> xLabels_m = new ArrayList<>(xLabelsMap_m.keySet());

        Collections.sort(xLabels_s); // 정렬
        Collections.sort(xLabels_m); // 정렬

        int prevSnoringCount = 0;
        int prevMovingCount = 0;

        Log.i(TAG, "xlabel_s size: " + xLabels_s.size());
        for (int i = 0; i < xLabels_s.size(); i++) {
            String hour = xLabels_s.get(i);
            String before_hour;
            String hour_before_test = null;

            if (i > 0) {
                before_hour = xLabels_s.get(i - 1);
                hour_before_test = before_hour.substring(0, 5);
            }

            String hour_now_test = hour.substring(0, 5);
            Log.i(TAG, "hour_now_test: " + hour_now_test);
            Log.i(TAG, "hour_before_test: " + hour_before_test);

            int snoringCount = maxSnoringByHour.containsKey(hour) ? maxSnoringByHour.get(hour) : 0;

            Log.i(TAG, "before compare prevSnoringCount: " + prevSnoringCount);

            // 시간대가 변경된 경우 0으로 초기화
            if (!Objects.equals(hour_now_test, hour_before_test)) {
                prevSnoringCount = 0;
                Log.i(TAG, "Active");
            }

            Log.i(TAG, "after compare prevSnoringCount: " + prevSnoringCount);

            // 현재 시간의 증가량 계산
            int snoringIncrement = snoringCount - prevSnoringCount;

            Log.i(TAG, "prevSnoringCount: " + prevSnoringCount);
            Log.i(TAG, "snoringCount: " + snoringCount);
            Log.i(TAG, "snoringIncrement: " + snoringIncrement);

            // snoring 값이 0이 아닌 경우에만 추가
            if (snoringIncrement > 0) {
                snoringEntries.add(new BarEntry(i, snoringIncrement));
                Log.i(TAG, "Added snoring value: " + snoringIncrement + " at hour: " + hour);
            }

            // snoring 값이 0인 경우 삭제
            if (snoringIncrement == 0) {
                xLabelsMap_s.remove(hour);
                Log.i(TAG, "Removed xLabel: " + hour + " because increments are 0");
            }

            // 이전 시간의 값 업데이트
            prevSnoringCount = snoringCount;

            Log.i(TAG, "finish prevSnoringCount: " + prevSnoringCount);
        }

        xLabels_s = new ArrayList<>(xLabelsMap_s.keySet());
        Collections.sort(xLabels_s);

        ArrayList<BarEntry> updatedSnoringEntries = new ArrayList<BarEntry>();
        for (BarEntry entry : snoringEntries) {
            if(entry.getY() != 0) {
                updatedSnoringEntries.add(entry);
            }
        }
        snoringEntries = updatedSnoringEntries;
        for (int i = 0; i < snoringEntries.size(); i++) {
            snoringEntries.get(i).setX(i);
        }

        for (int i = 0; i < xLabels_m.size(); i++) {
            String hour = xLabels_m.get(i);
            String before_hour;
            String hour_before_test = null;

            if (i > 0) {
                before_hour = xLabels_m.get(i - 1);
                hour_before_test = before_hour.substring(0, 5);
            }

            String hour_now_test = hour.substring(0, 5);
            Log.i(TAG, "hour_now_test: " + hour_now_test);
            Log.i(TAG, "hour_before_test: " + hour_before_test);

            int movingCount = maxMovingByHour.containsKey(hour) ? maxMovingByHour.get(hour) : 0;

            Log.i(TAG, "before compare prevMovingCount: " + prevMovingCount);

            // 시간대가 변경된 경우 0으로 초기화
            if (!Objects.equals(hour_now_test, hour_before_test)) {
                prevMovingCount = 0;
                Log.i(TAG, "Active");
            }

            Log.i(TAG, "after compare prevMovingCount: " + prevMovingCount);

            // 현재 시간의 증가량 계산
            int movingIncrement = movingCount - prevMovingCount;

            Log.i(TAG, "prevMovingCount: " + prevMovingCount);
            Log.i(TAG, "movingCount: " + movingCount);
            Log.i(TAG, "movingIncrement: " + movingIncrement);

            // moving 값이 0이 아닌 경우에만 추가
            if (movingIncrement > 0) {
                movingEntries.add(new BarEntry(i, movingIncrement));
                Log.i(TAG, "Added moving value: " + movingIncrement + " at hour: " + hour);
            }

            // moving 값이 0인 경우 삭제
            if (movingIncrement == 0) {
                xLabelsMap_m.remove(hour);
                Log.i(TAG, "Removed xLabel: " + hour + " because increments are 0");
            }

            // 이전 시간의 값 업데이트
            prevMovingCount = movingCount;

            Log.i(TAG, "finish prevMovingCount: " + prevMovingCount);
        }

        xLabels_m = new ArrayList<>(xLabelsMap_m.keySet());
        Collections.sort(xLabels_m);

        ArrayList<BarEntry> updatedMovingEntries = new ArrayList<BarEntry>();
        for (BarEntry entry : movingEntries) {
            if(entry.getY() != 0) {
                updatedMovingEntries.add(entry);
            }
        }
        movingEntries = updatedMovingEntries;
        for (int i = 0; i < movingEntries.size(); i++) {
            movingEntries.get(i).setX(i);
        }

        // 로그 출력
        Log.i(TAG, "Snoring Entries: " + snoringEntries.toString());
        Log.i(TAG, "Moving Entries: " + movingEntries.toString());
        Log.i(TAG, "X_s Labels: " + xLabels_s.toString());
        Log.i(TAG, "X_m Labels: " + xLabels_m.toString());

        // 그래프 초기화
        BarChart snoringBarChart = activity.findViewById(R.id.snoring_chart);
        BarChart movingBarChart = activity.findViewById(R.id.moving_chart);

        setupBarChart(snoringBarChart, "코골이 그래프", snoringEntries, xLabels_s);
        setupBarChart(movingBarChart, "뒤척임 그래프", movingEntries, xLabels_m);
    }

    // 그래프 설정 메서드
    private void setupBarChart(BarChart chart, String description, ArrayList<BarEntry> entries, ArrayList<String> xLabels) {
        BarDataSet dataSet = new BarDataSet(entries, description);
        BarData barData = new BarData(dataSet);

        dataSet.setColor(Color.rgb(68, 65, 101));
        dataSet.setValueTextSize(15);
        dataSet.setValueFormatter(new CustomValueFormatter()); // 여기서 CustomValueFormatter를 설정

        barData.setBarWidth(0.5f);

        chart.setData(barData);
        chart.getDescription().setText(description);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getAxisRight().setEnabled(false);
        chart.setVisibleXRangeMaximum(7);

        XAxis xAxis = chart.getXAxis();
        xAxis.setDrawLabels(true);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));
        xAxis.setTextSize(12);
        xAxis.enableGridDashedLine(10, 24, 0);
        xAxis.setTextColor(Color.rgb(51, 51, 51));

        chart.getAxisLeft().setAxisMinimum(0);

        chart.animateX(1000);
        chart.animateY(1000);

        chart.invalidate();
    }

    // JSON 문자열을 파싱하여 태그 목록을 반환하는 메서드
    protected ArrayList<Tag> getArrayListFromJSONString(String jsonString) {
        ArrayList<Tag> output = new ArrayList<>();
        try {
            if (jsonString.length() > 2) {
                jsonString = jsonString.substring(1, jsonString.length() - 1);
                jsonString = jsonString.replace("\\\"", "\"");

                Log.i(TAG, "jsonString=" + jsonString);

                JSONObject root = new JSONObject(jsonString);
                JSONArray jsonArray = root.getJSONArray("data");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    Tag tag = new Tag(
                            jsonObject.getString("posture"),
                            jsonObject.getString("snoring"),
                            jsonObject.getString("startTime"),
                            jsonObject.getString("endTime"),
                            jsonObject.getString("moving"),
                            jsonObject.getString("timestamp")
                    );

                    output.add(tag);
                }
            } else {
                Log.e(TAG, "jsonString이 너무 짧습니다.");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return output;
    }
    // Tag 클래스는 각 로그 항목을 나타냄
    class Tag {
        String startTime;
        String endTime;
        String snoring;
        String posture;
        String moving;
        String timestamp;

        public Tag(String posture, String snoring, String startTime, String endTime, String moving, String timestamp) {
            this.posture = posture;
            this.snoring = snoring;
            this.startTime = startTime;
            this.endTime = endTime;
            this.moving = moving;
            this.timestamp = timestamp;
        }

        public String toString() {
            return String.format("[%s] Moving: %s, Posture: %s, Snoring: %s, Start: %s, End: %s", timestamp, moving, posture, snoring, startTime, endTime);
        }
    }

    // CustomValueFormatter 클래스 추가
    // 그래프에 값+회 되게끔 수정
    class CustomValueFormatter extends ValueFormatter {
        @Override
        public String getBarLabel(BarEntry barEntry) {
            // 값과 "회" 단어를 결합하여 반환
            int value = (int) barEntry.getY(); // 소수점 제거를 위해 int로 캐스팅
            return value + "회";
        }
    }
}

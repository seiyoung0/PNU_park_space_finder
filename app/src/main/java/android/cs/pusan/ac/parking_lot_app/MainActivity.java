package android.cs.pusan.ac.parking_lot_app;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private Button[] building_button = null;
    private Button test;
    private String strjson;
    private static final long START_TIME = 20999;
    private long TimeLeft = START_TIME;
    private Button timer_button;
    private CountDownTimer timer;
    //private int[][] building_num_capacity = {{311, 24}, {312, 24}, {313, 24}};
    private int[][] building_num_capacity = {{207, 20}, {208, 15}, {311, 24}, {312, 24}, {313, 24}, {401, 12}, {407, 18}, {419, 20}, {503, 25}, {507, 22}, {514, 20}, {603, 18}, {704, 16}, {713, 14}};
    private int[] building_usage = new int[building_num_capacity.length];

    private String apiurl = "http://the4456.iptime.org:3030/app?0";
    private OkHttpClient client;
    private Request request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("PNU parking space finder");

        client = new OkHttpClient();
        request = new Request.Builder().url(apiurl).build();

        building_button = new Button[building_num_capacity.length];
        for (int i = 0; i < building_num_capacity.length; i++){
            String btn = "b" + Integer.toString(building_num_capacity[i][0]);
            int resIDbtn = getResources().getIdentifier(btn, "id", getPackageName());
            building_button[i] = findViewById(resIDbtn);
        }

        for (int i = 0; i < building_num_capacity.length; i++) {
            String building_activity = "building" + building_num_capacity[i][0];
            building_button[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = null;
                    try {
                        intent = new Intent(MainActivity.this, Class.forName("android.cs.pusan.ac.parking_lot_app."+building_activity));
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    startActivity(intent);
                }
            });
        }

        timer_button = findViewById(R.id.timer);
        startTimer();
        GetDataFromSQL();
        timer_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                resetTimer();
            }
        });
    }
    private void startTimer(){
        timer = new CountDownTimer(TimeLeft, 1000) {
            @Override
            public void onTick(long l) {
                TimeLeft = l;
                updateCountDownText();
            }
            @Override
            public void onFinish() {
                resetTimer();
            }
        }.start();
    }
    private void resetTimer(){
        timer.cancel();
        TimeLeft = START_TIME;
        updateCountDownText();
        startTimer();
        Arrays.fill(building_usage,0);
        GetDataFromSQL();
    }
    private void updateCountDownText(){
        int seconds = (int)TimeLeft/1000;
        String TimeLeftSTR = String.valueOf(seconds);
        timer_button.setText(TimeLeftSTR);
    }

    public void GetDataFromSQL(){
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()){
                    strjson = response.body().string();
                    StringBuffer buffer = new StringBuffer();
                    buffer.append(strjson);

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONArray jsonArr = new JSONArray(buffer.toString());
                                int jsonArr_size = jsonArr.length();
                                JSONObject jsonObj[] = new JSONObject[jsonArr_size];
                                for (int i = 0; i < jsonArr_size; i++){
                                    jsonObj[i] = jsonArr.getJSONObject(i);
                                    int bn = jsonObj[i].getInt("building_number");
                                    String oc = jsonObj[i].getString("occupied");
                                    for (int j = 0; j < building_num_capacity.length; j++) {
                                        if (building_num_capacity[j][0] == bn && oc.equals("1")) {
                                            building_usage[j]++;
                                        }
                                    }
                                }
                                for (int i = 0; i < building_num_capacity.length; i++) {
                                    colorbynum(building_usage[i], building_num_capacity[i][1], building_button[i]);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
    }
    public void colorbynum(int building_usage, int building_capacity, Button building_button){
        int color = 0;
        if (building_usage <= building_capacity/9) color = Color.parseColor("#49B577");
        else if (building_usage <= (building_capacity * 2)/9) color = Color.parseColor("#71B16B");
        else if (building_usage <= (building_capacity * 3)/9) color = Color.parseColor("#9DAC5E");
        else if (building_usage <= (building_capacity * 4)/9) color = Color.parseColor("#C5A852");
        else if (building_usage <= (building_capacity * 5)/9) color = Color.parseColor("#F1A344");
        else if (building_usage <= (building_capacity * 6)/9) color = Color.parseColor("#E4853F");
        else if (building_usage <= (building_capacity * 7)/9) color = Color.parseColor("#D6643A");
        else if (building_usage <= (building_capacity * 8)/9) color = Color.parseColor("#CA4536");
        else if (building_usage <= building_capacity) color = Color.parseColor("#BC2431");
        @SuppressLint("UseCompatLoadingForDrawables") Drawable circle_button = getResources().getDrawable(R.drawable.circle_button);
        circle_button.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        building_button.setBackground(circle_button);
    }
}
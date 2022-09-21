package android.cs.pusan.ac.parking_lot_app;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
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
import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;

public class building407 extends AppCompatActivity {
    private Button[] ps = null;
    private String strjson;
    private static final long START_TIME = 20999;
    private long TimeLeft = START_TIME;
    private Button timer_button;
    private CountDownTimer timer;

    private String apiurl = "http://the4456.iptime.org:3030/app?407";
    private OkHttpClient client;
    private Request request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_building407);
        getSupportActionBar().setTitle("Building 407");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //뒤로가기 버튼

        //버튼, 버튼 아이디 선언하고 for문으로 부여.
        ps = new Button[24];
        for (int i = 0; i < 24; i++){
            String btn = "ps"+(i+1);
            int resIDbtn = getResources().getIdentifier(btn, "id", getPackageName());
            ps[i] = findViewById(resIDbtn);
        }

        client = new OkHttpClient();
        request = new Request.Builder().url(apiurl).build();

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
        GetDataFromSQL();
    }
    private void updateCountDownText(){
        int seconds = (int)TimeLeft/1000;
        String TimeLeftSTR = String.valueOf(seconds);
        timer_button.setText(TimeLeftSTR);
    }

    @Override
    public boolean onSupportNavigateUp() {
        timer.cancel();
        this.finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        timer.cancel();
        this.finish();
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

                    building407.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONArray jsonArr = new JSONArray(buffer.toString());
                                int jsonArr_size = jsonArr.length();
                                JSONObject jsonObj[] = new JSONObject[jsonArr_size];
                                for (int i = 0; i < jsonArr_size; i++){
                                    jsonObj[i] = jsonArr.getJSONObject(i);
                                    String oc = jsonObj[i].getString("occupied");
                                    int space_num = jsonObj[i].getInt("parking_lot_number");
                                    if (oc.equals("1")) ps[space_num-1].setBackgroundColor(Color.parseColor("#BC2431"));
                                    else ps[space_num-1].setBackgroundColor(Color.parseColor("#49B577"));
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
}
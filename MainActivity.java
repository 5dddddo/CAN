package com.example.t4ir_final;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {
    String userId;
    String userPw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button loginBtn = (Button) findViewById(R.id.loginBtn);
        Button findIdPwBtn = (Button) findViewById(R.id.findIdPwBtn);
        Button addUserBtn = (Button) findViewById(R.id.addUserBtn);

        TextView idEditView = (TextView) findViewById(R.id.idEditView);
        TextView pwEditView = (TextView) findViewById(R.id.pwEditView);

        userId = idEditView.getText().toString();
        userPw = pwEditView.getText().toString();


        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 로그인 처리 통신

                // 로그인 성공
                Intent i = new Intent();
                i.putExtra("userId", userId);
                i.putExtra("userPw", userPw);
                ComponentName cname = new ComponentName("com.example.t4ir_final", "com.example.t4ir_final.HomeActivity");
                i.setComponent(cname);
                startActivity(i);

                // 로그인 실패
            }
        });


    }
}

package com.example.bactopedia;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class BacteriaInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bacteria_info);

        // Панель навигации и строка состояния
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.bg)); // цвет строки состояния
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.bg)); // цвет панели навигации

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // цвет значков строки состояния
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // цвет значков панели навигации
                int flags = window.getDecorView().getSystemUiVisibility();
                flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                window.getDecorView().setSystemUiVisibility(flags);
            }
        }


        String selectedBacteria = getIntent().getStringExtra("selectedBacteria");
        String bacteriaInfo = getIntent().getStringExtra("bacteriaInfo");
        int bacteriaShowImage = getIntent().getIntExtra("bacteriaShowImage", 0);

        TextView bacteriaNameTextView = findViewById(R.id.print_bacteria_name);
        TextView bacteriaInfoTextView = findViewById(R.id.print_bacteria_info);
        ImageView bacteriaImageView = findViewById(R.id.print_bacteria_image);

        bacteriaNameTextView.setText(selectedBacteria);
        bacteriaInfoTextView.setText(bacteriaInfo);
        bacteriaImageView.setImageResource(bacteriaShowImage);
    }


}

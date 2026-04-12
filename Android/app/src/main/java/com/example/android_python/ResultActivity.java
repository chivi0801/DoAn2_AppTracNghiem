package com.example.android_python;

import static android.content.Intent.getIntent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ImageView imageView = new ImageView(this);
        setContentView(imageView);

        String imagePath = getIntent().getStringExtra("PATH_ANH_CHINH");

        if (imagePath != null) {
            File imgFile = new File(imagePath);

            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                imageView.setImageBitmap(myBitmap);
            } else {
                Toast.makeText(this, "File không tồn tại", Toast.LENGTH_SHORT).show();
            }

        }
    }
}
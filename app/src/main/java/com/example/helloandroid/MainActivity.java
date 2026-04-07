package com.example.helloandroid;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private TextView resultText;
    private ModelRunner modelRunner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resultText = findViewById(R.id.resultText);
        Button runButton = findViewById(R.id.runButton);

        modelRunner = new ModelRunner(this);

        runButton.setOnClickListener(v -> {
            try {
                // 1. assets 에서 이미지 로드
                InputStream is = getAssets().open("test.jpg");
                Bitmap bitmap = BitmapFactory.decodeStream(is);

                // 2. 전처리
                float[] input = bitmapToInput(bitmap);

                // 3. 모델 실행
                float[] output = modelRunner.run(input);

                // 4. 결과 해석
                int bestIdx = argmax(output);
                float confidence = output[bestIdx];

                // 5. 화면 출력
                resultText.setText(
                        "Prediction Index: " + bestIdx +
                                "\nConfidence: " + confidence
                );

            } catch (Exception e) {
                e.printStackTrace();
                resultText.setText("Error: " + e.getMessage());
            }
        });
    }

    // 🔹 Bitmap → 모델 입력 변환
    private float[] bitmapToInput(Bitmap bitmap) {
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true);

        int[] pixels = new int[224 * 224];
        resized.getPixels(pixels, 0, 224, 0, 0, 224, 224);

        float[] input = new float[224 * 224 * 3];
        int idx = 0;

        for (int pixel : pixels) {
            float r = ((pixel >> 16) & 0xFF) / 255f;
            float g = ((pixel >> 8) & 0xFF) / 255f;
            float b = (pixel & 0xFF) / 255f;

            input[idx++] = r;
            input[idx++] = g;
            input[idx++] = b;
        }

        return input;
    }

    // 🔹 최대값 인덱스 찾기
    private int argmax(float[] values) {
        int bestIdx = 0;
        float bestVal = values[0];

        for (int i = 1; i < values.length; i++) {
            if (values[i] > bestVal) {
                bestVal = values[i];
                bestIdx = i;
            }
        }
        return bestIdx;
    }
}
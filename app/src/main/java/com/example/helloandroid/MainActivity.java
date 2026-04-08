package com.example.helloandroid;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView resultText;
    private ModelRunner modelRunner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resultText = findViewById(R.id.resultText);
        Button runButton = findViewById(R.id.runButton);

        try {
            modelRunner = new ModelRunner(this);
        } catch (Exception e) {
            resultText.setText("Model load failed: " + e.getMessage());
            return;
        }

        runButton.setOnClickListener(v -> {
            try {
                float[] input = new float[1 * 3 * 224 * 224];
                long[] shape = new long[]{1, 3, 224, 224};

                float[] output = modelRunner.run(input, shape);
                resultText.setText("Output length: " + output.length);
            } catch (Exception e) {
                resultText.setText("Inference failed: " + e.getMessage());
            }
        });
    }
}
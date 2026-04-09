package com.example.helloandroid;

import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private TextView resultText;
    private ImageView previewImage;
    private ModelRunner modelRunner;
    private Bitmap selectedBitmap;
    private List<String> labels;
    private ExecutorService inferenceExecutor;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher;
    private ActivityResultLauncher<String[]> pickImageFileLauncher;

    private static final int INPUT_SIZE = 224;
    private static final int TOP_K = 5;
    private static final float[] IMAGENET_MEAN = {0.485f, 0.456f, 0.406f};
    private static final float[] IMAGENET_STD = {0.229f, 0.224f, 0.225f};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resultText = findViewById(R.id.resultText);
        previewImage = findViewById(R.id.previewImage);
        Button pickImageButton = findViewById(R.id.pickImageButton);
        Button pickImageFileButton = findViewById(R.id.pickImageFileButton);
        Button runButton = findViewById(R.id.runButton);
        runButton.setEnabled(false);

        inferenceExecutor = Executors.newSingleThreadExecutor();
        labels = loadLabels();

        pickMediaLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                this::onImagePicked
        );
        pickImageFileLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                this::onImagePicked
        );

        try {
            modelRunner = new ModelRunner(this);
        } catch (Exception e) {
            resultText.setText("Model load failed: " + e.getMessage());
            pickImageButton.setEnabled(false);
            pickImageFileButton.setEnabled(false);
            runButton.setEnabled(false);
            return;
        }

        pickImageButton.setOnClickListener(v -> pickMediaLauncher.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()
        ));
        pickImageFileButton.setOnClickListener(v ->
                pickImageFileLauncher.launch(new String[]{"image/*"}));

        runButton.setOnClickListener(v -> {
            if (selectedBitmap == null) {
                resultText.setText("Please pick a photo first.");
                return;
            }
            runButton.setEnabled(false);
            resultText.setText("Running inference...");
            runInference(selectedBitmap, runButton);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (inferenceExecutor != null) {
            inferenceExecutor.shutdown();
        }
    }

    private void onImagePicked(Uri uri) {
        if (uri == null) {
            resultText.setText("Photo selection canceled.");
            return;
        }
        try {
            Bitmap bitmap;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), uri);
                bitmap = ImageDecoder.decodeBitmap(source);
            } else {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            }
            selectedBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false);
            previewImage.setImageBitmap(selectedBitmap);
            resultText.setText("Photo selected. Tap Run Model.");
            findViewById(R.id.runButton).setEnabled(true);
        } catch (IOException e) {
            selectedBitmap = null;
            resultText.setText("Failed to read image: " + e.getMessage());
            findViewById(R.id.runButton).setEnabled(false);
        }
    }

    private void runInference(Bitmap bitmap, Button runButton) {
        inferenceExecutor.execute(() -> {
            try {
                float[] input = preprocessBitmap(bitmap);
                long[] shape = new long[]{1, 3, INPUT_SIZE, INPUT_SIZE};
                float[] logits = modelRunner.run(input, shape);
                float[] probs = softmax(logits);
                String summary = buildTopKResult(probs, TOP_K);

                runOnUiThread(() -> {
                    resultText.setText(summary);
                    runButton.setEnabled(true);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    resultText.setText("Inference failed: " + e.getMessage());
                    runButton.setEnabled(true);
                });
            }
        });
    }

    private float[] preprocessBitmap(Bitmap bitmap) {
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true);
        int[] pixels = new int[INPUT_SIZE * INPUT_SIZE];
        resized.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE);

        float[] input = new float[1 * 3 * INPUT_SIZE * INPUT_SIZE];
        int planeSize = INPUT_SIZE * INPUT_SIZE;
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            float r = ((pixel >> 16) & 0xFF) / 255.0f;
            float g = ((pixel >> 8) & 0xFF) / 255.0f;
            float b = (pixel & 0xFF) / 255.0f;

            input[i] = (r - IMAGENET_MEAN[0]) / IMAGENET_STD[0];
            input[planeSize + i] = (g - IMAGENET_MEAN[1]) / IMAGENET_STD[1];
            input[(2 * planeSize) + i] = (b - IMAGENET_MEAN[2]) / IMAGENET_STD[2];
        }
        return input;
    }

    private float[] softmax(float[] logits) {
        float max = logits[0];
        for (float value : logits) {
            if (value > max) {
                max = value;
            }
        }

        float sum = 0f;
        float[] exp = new float[logits.length];
        for (int i = 0; i < logits.length; i++) {
            exp[i] = (float) Math.exp(logits[i] - max);
            sum += exp[i];
        }
        for (int i = 0; i < exp.length; i++) {
            exp[i] = exp[i] / sum;
        }
        return exp;
    }

    private String buildTopKResult(float[] probabilities, int k) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < probabilities.length; i++) {
            indices.add(i);
        }
        indices.sort(Comparator.comparingDouble((Integer idx) -> probabilities[idx]).reversed());

        StringBuilder sb = new StringBuilder();
        int topCount = Math.min(k, indices.size());
        if (topCount > 0) {
            int top1 = indices.get(0);
            sb.append("Top-1: ")
                    .append(labelFor(top1))
                    .append(" (")
                    .append(formatPercent(probabilities[top1]))
                    .append(")\n\n");
        }

        sb.append("Top-").append(topCount).append(":\n");
        for (int rank = 0; rank < topCount; rank++) {
            int idx = indices.get(rank);
            sb.append(rank + 1)
                    .append(". ")
                    .append(labelFor(idx))
                    .append(" - ")
                    .append(formatPercent(probabilities[idx]));
            if (rank < topCount - 1) {
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    private String formatPercent(float probability) {
        return String.format(Locale.US, "%.2f%%", probability * 100f);
    }

    private String labelFor(int index) {
        if (index >= 0 && index < labels.size()) {
            return labels.get(index);
        }
        return "Class " + index;
    }

    private List<String> loadLabels() {
        List<String> loaded = tryLoadLabelsFile("labels.txt");
        if (loaded.isEmpty()) {
            loaded = tryLoadLabelsFile("ImageNetLabels.txt");
        }

        if (loaded.size() == 1001 && "background".equalsIgnoreCase(loaded.get(0))) {
            loaded = new ArrayList<>(loaded.subList(1, loaded.size()));
        }

        if (!loaded.isEmpty()) {
            return loaded;
        }

        List<String> fallback = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            fallback.add("Class " + i);
        }
        return fallback;
    }

    private List<String> tryLoadLabelsFile(String assetFileName) {
        try (InputStream is = getAssets().open(assetFileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            List<String> loaded = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String label = line.trim();
                if (!label.isEmpty()) {
                    loaded.add(label);
                }
            }
            return loaded;
        } catch (IOException ignored) {
            return new ArrayList<>();
        }
    }
}

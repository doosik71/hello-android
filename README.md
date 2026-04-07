# 📱 Android + ExecuTorch 딥러닝 모델 실행 실습

이 문서는 **Hugging Face / PyTorch 모델을 ExecuTorch로 변환하여 Android(Java) 앱에서 실행하는 방법**을 단계별로 설명합니다.

## 🎯 목표

* PyTorch / Hugging Face 모델을 `.pte`로 변환
* Android 앱에서 모델 로드 및 추론 실행
* Java 기반 Android 프로젝트에서 딥러닝 실행 성공

## 🧱 전체 흐름

```text
Hugging Face 모델
        ↓
ExecuTorch로 변환 (.pte)
        ↓
Android 프로젝트 assets에 추가
        ↓
Java 코드에서 로드 및 실행
```

## 🖥️ 1. PC 환경 준비

### 1. Python 환경 생성

```bash
python -m venv venv
source venv/bin/activate   ## Windows: venv\Scripts\activate
```

### 2. ExecuTorch 설치

```bash
pip install executorch
```

### 3. Hugging Face 변환 도구 설치

```bash
git clone https://github.com/huggingface/optimum-executorch.git
cd optimum-executorch
pip install .
```

## 🤖 2. Hugging Face 모델 변환

### 기본 명령어

```bash
optimum-cli export executorch \
  --model <MODEL_ID> \
  --task <TASK> \
  --recipe xnnpack \
  --output_dir exported_model
```

### 예시

```bash
optimum-cli export executorch \
  --model HuggingFaceTB/SmolLM2-135M \
  --task text-generation \
  --recipe xnnpack \
  --output_dir exported_model
```

### 결과

```text
exported_model/
 └─ model.pte   ← 이 파일 사용
```

## 📦 3. Android 프로젝트 설정

### 1. 의존성 추가

`app/build.gradle`

```gradle
dependencies {
    implementation("org.pytorch:executorch-android:1.0.0")
}
```

### 2. Gradle Sync

```text
File → Sync Project with Gradle Files
```

### 3. 모델 파일 추가

```text
app/src/main/assets/model.pte
```

## 🧠 4. ModelRunner.java

```java
package com.example.helloandroid;

import android.content.Context;

import org.pytorch.executorch.EValue;
import org.pytorch.executorch.Module;
import org.pytorch.executorch.Tensor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ModelRunner {

    private final Module module;

    public ModelRunner(Context context) throws Exception {
        String modelPath = assetFilePath(context, "model.pte");
        module = Module.load(modelPath);
    }

    public float[] run(float[] input, long[] shape) {
        Tensor inputTensor = Tensor.fromBlob(input, shape);
        EValue[] outputs = module.forward(EValue.from(inputTensor));
        return outputs[0].toTensor().getDataAsFloatArray();
    }

    private static String assetFilePath(Context context, String assetName) throws Exception {
        File file = new File(context.getFilesDir(), assetName);

        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName);
             FileOutputStream os = new FileOutputStream(file)) {

            byte[] buffer = new byte[4096];
            int read;
            while ((read = is.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
        }

        return file.getAbsolutePath();
    }
}
```

## 📱 5. MainActivity.java

```java
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
```

## 🖼️ 6. activity_main.xml

```xml
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="vertical"
    android:padding="16dp"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <Button
        android:id="@+id/runButton"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Run Model" />

    <TextView
        android:id="@+id/resultText"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Result will appear here"
        android:paddingTop="20dp" />
</LinearLayout>
```

## 📁 프로젝트 구조

```text
app/
 └─ src/
     └─ main/
         ├─ java/com/example/helloandroid/
         │   ├─ MainActivity.java
         │   └─ ModelRunner.java
         │
         ├─ res/layout/
         │   └─ activity_main.xml
         │
         └─ assets/
             └─ model.pte
```

## 🚀 실행 방법

1. 앱 실행
2. "Run Model" 버튼 클릭
3. 결과 출력 확인

## ⚠️ 주의사항

### 1. 모델 입력 shape 맞추기

```java
long[] shape = new long[]{1, 3, 224, 224};
```

👉 export할 때 사용한 shape와 동일해야 함

### 2. 모델마다 전처리 다름

* normalization 필요할 수 있음
* 채널 순서 (NCHW vs NHWC)

### 3. 지원되지 않는 모델

* 일부 Hugging Face 모델은 export 실패
* Optimum ExecuTorch 지원 모델만 사용

### 4. 처음에는 CPU (xnnpack) 사용

```bash
--recipe xnnpack
```

## 🔥 추천 실습 순서

1. torchvision MobileNetV2로 `.pte` 생성
2. Android에서 실행 성공
3. Hugging Face 모델로 확장

## 💡 확장 아이디어

* 카메라 입력 연결
* 실시간 추론
* GPU / NPU 가속
* UI 개선

## 🧾 한 줄 요약

👉 **PyTorch 모델 → ExecuTorch(.pte) → Android(Java)에서 실행**

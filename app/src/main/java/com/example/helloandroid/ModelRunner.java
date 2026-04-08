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
            os.flush();
        }

        return file.getAbsolutePath();
    }
}
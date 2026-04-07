package com.example.helloandroid;

import android.content.Context;
import com.google.ai.edge.litert.Accelerator;
import com.google.ai.edge.litert.CompiledModel;

public class ModelRunner {

    private CompiledModel model;

    public ModelRunner(Context context) {
        model = CompiledModel.create(
                context.getAssets(),
                "mobilenet_v2.tflite",
                new CompiledModel.Options(Accelerator.CPU)
        );
    }

    public float[] run(float[] input) {
        java.util.List<com.google.ai.edge.litert.TensorBuffer> inputBuffers =
                model.createInputBuffers();

        java.util.List<com.google.ai.edge.litert.TensorBuffer> outputBuffers =
                model.createOutputBuffers();;

        inputBuffers.get(0).writeFloat(input);
        model.run(inputBuffers, outputBuffers);

        return outputBuffers.get(0).readFloat();
    }
}
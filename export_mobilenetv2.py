from executorch.backends.xnnpack.partition.xnnpack_partitioner import XnnpackPartitioner
from executorch.exir import to_edge_transform_and_lower
from torch.export import export
from torchvision.models.mobilenetv2 import MobileNet_V2_Weights
import os
import torch
import torchvision.models as models


# Android 프로젝트 경로 (수정 필요!)
ANDROID_PROJECT_ROOT = "./"

OUTPUT_PATH = os.path.join(
    ANDROID_PROJECT_ROOT,
    "app/src/main/assets/model.pte"
)

# 1) pretrained MobileNetV2 로드
model = models.mobilenet_v2(weights=MobileNet_V2_Weights.DEFAULT).eval()

# 2) 샘플 입력
sample_inputs = (torch.randn(1, 3, 224, 224),)

# 3) PyTorch export
exported_program = export(model, sample_inputs)

# 4) XNNPACK lowering
edge_program = to_edge_transform_and_lower(
    exported_program,
    partitioner=[XnnpackPartitioner()],
)

# 5) ExecuTorch 프로그램 생성
executorch_program = edge_program.to_executorch()

# 🔹 assets 폴더 없으면 생성
os.makedirs(os.path.dirname(OUTPUT_PATH), exist_ok=True)

# 6) 파일 저장
with open(OUTPUT_PATH, "wb") as f:
    f.write(executorch_program.buffer)

print(f"Saved model to: {OUTPUT_PATH}")

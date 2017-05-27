# GPUImage for Android with Video Recording feature
[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

Idea from: [iOS GPUImage framework](https://github.com/BradLarson/GPUImage)
Base on: [Android GPUImage framework](https://github.com/CyberAgent/android-gpuimage)

## Requirements
* Android 4.3 or higher (OpenGL ES 2.0)

## Concept
1. draw on current screen surface
2. switch to encoder input surface and draw previous frame buffer again on it
3. switch back to screen surface

## Reference
1. EGL surface helper: https://github.com/google/grafika
2. Media encoder: https://github.com/saki4510t/AudioVideoRecordingSample
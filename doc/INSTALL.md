# Android Studio 설치 및 가상 기기 설정 가이드

이 문서는 `hello-android` 프로젝트를 실행하기 위한 개발 환경 구축 방법을 설명합니다.

## 1. Android Studio 설치 방법

### Windows / macOS / Linux 공통

1. **공식 다운로드 페이지 접속**: [developer.android.com/studio](https://developer.android.com/studio)에 접속합니다.
2. **Download Android Studio 클릭**: 최신 버전의 설치 파일을 다운로드합니다.
3. **설치 프로그램 실행**:
   - **Windows**: `.exe` 파일을 실행하여 설치 마법사의 안내를 따릅니다.
   - **macOS**: `.dmg` 파일을 열고 Android Studio 아이콘을 `Applications` 폴더로 드래그합니다.
   - **Linux**: `.tar.gz` 압축을 풀고 `bin/studio.sh`를 실행합니다.
4. **Setup Wizard 실행**:
   - Android Studio를 처음 실행하면 'Setup Wizard'가 나타납니다.
   - **Standard** 설치 타입을 선택하여 기본 SDK, 플랫폼 도구, 빌드 도구를 설치합니다.
   - 설치 과정에서 사용자의 지역과 UI 테마(Light/Dark)를 선택합니다.
5. **SDK 설치 완료**: 필요한 컴포넌트 다운로드가 완료될 때까지 기다린 후 `Finish`를 클릭합니다.

## 2. 가상 기기(Virtual Device) 추가 방법

실제 안드로이드 기기가 없는 경우, Android Studio의 **Device Manager**를 통해 에뮬레이터를 생성할 수 있습니다.

### 단계별 설정 순서

1. **Device Manager 열기**:
   - Android Studio 메인 화면 우측 툴바에서 **Device Manager** 아이콘을 클릭합니다.
   - 또는 상단 메뉴에서 `Tools` $\rightarrow$ `Device Manager`를 선택합니다.
2. **가상 기기 생성 시작**:
   - Device Manager 창에서 **Create Device** 버튼을 클릭합니다.
3. **하드웨어 선택 (Select Hardware)**:
   - 원하는 기기 형태를 선택합니다 (예: `Phone` $\rightarrow$ `Pixel 7`).
   - 선택 후 `Next`를 클릭합니다.
4. **시스템 이미지 선택 (Select System Image)**:
   - 앱이 실행될 안드로이드 OS 버전을 선택합니다 (예: `API 34` / `UpsideDownCake`).
   - 선택한 버전 옆에 `Download` 링크가 있다면 클릭하여 이미지를 먼저 다운로드해야 합니다.
   - 다운로드가 완료되면 해당 버전을 선택하고 `Next`를 클릭합니다.
5. **최종 설정 및 완료 (Verify Configuration)**:
   - 가상 기기의 이름(AVD Name)을 확인하거나 수정합니다.
   - `Finish`를 클릭하면 가상 기기 생성이 완료됩니다.

### 가상 기기 실행

- Device Manager 목록에서 생성한 기기 옆의 **재생($\blacktriangleright$) 버튼**을 클릭하면 에뮬레이터가 구동됩니다.
- 에뮬레이터가 켜진 상태에서 상단 툴바의 실행 버튼을 누르면 작성한 앱이 가상 기기에 설치됩니다.

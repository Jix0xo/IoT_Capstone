# 🛌 Resty
스마트베개 Resty는 코골이와 뒤척임을 실시간으로 감지하고 완화하며, 올바른 수면 자세를 유도하는 제품이다. 마이크와 AI 모델로 코골이 소리를 감지하고 자세를 조정하여 수면의 질을 향상시킨다. 수면 데이터를 분석해 사용자가 자신의 수면 패턴을 이해하고 개선할 수 있도록 돕는다.

## 목차
- [프로젝트 선정 이유](#프로젝트-선정-이유)
- [팀 구성](#팀-구성)
- [기능설명](#기능-설명)
- [기술 및 구조](#기술-및-구조)
- [기능 시연](#기능-시연)

## 프로젝트 선정 이유
잠이 보약이란 말이 있듯이 삶에서 잠은 중요한 요소 중 하나이다. 우린 중요한 요소 중 하나인 수면의 질을 간편하게 향상시키고자 이 프로젝트를 진행하게됐다.

## 팀 구성

|역할|이름|담당 업무|
|---|:----:|:---------:|
|팀장|권지오|AWS IoT + 앱|
|팀원|김형준|디바이스 제작|
|팀원|유란|코골이 딥러닝 모델 + 라즈베리파이|

## 기능 설명
### 주요 기능 3가지

- 코골이 완화 기능
  - 라즈베리파이에 있는 딥러닝 모델을 통해 코골이 인식 후 베개 내부 에어펌프가 자세 교정
  
- 자세 교정 기능
  - 베개 내부 압력 센서를 통해 뒤척임을 감지 후 베개 내부 에어펌프가 자세 교정
- 앱을 통한 설정 및 수면 중 통계 확인
  - 자신이 원하는 옵션 설정 및 수면 중 뒤척임, 코골이 횟수 확인 가능

## 기술 및 구조
- 개발 언어 : 
![C++](https://img.shields.io/badge/c++-%2300599C.svg?style=for-the-badge&logo=c%2B%2B&logoColor=white)
![Python](https://img.shields.io/badge/python-3670A0?style=for-the-badge&logo=python&logoColor=ffdd54)
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)  

- 개발 도구 : ![Android Studio](https://img.shields.io/badge/android%20studio-346ac1?style=for-the-badge&logo=android%20studio&logoColor=white)
![Jupyter Notebook](https://img.shields.io/badge/jupyter-%23FA0F00.svg?style=for-the-badge&logo=jupyter&logoColor=white)
![AWS](https://img.shields.io/badge/AWS-%23FF9900.svg?style=for-the-badge&logo=amazon-aws&logoColor=white)
![TensorFlow](https://img.shields.io/badge/TensorFlow-%23FF6F00.svg?style=for-the-badge&logo=TensorFlow&logoColor=white)
![IntelliJ IDEA](https://img.shields.io/badge/IntelliJIDEA-000000.svg?style=for-the-badge&logo=intellij-idea&logoColor=white)  

- 디바이스 : 
 ![Arduino](https://img.shields.io/badge/-Arduino-00979D?style=for-the-badge&logo=Arduino&logoColor=white)
![Raspberry Pi](https://img.shields.io/badge/-RaspberryPi-C51A4A?style=for-the-badge&logo=Raspberry-Pi)
- 시스템 구조 :<br/>
<br/>

![구조도](https://github.com/Jix0xo/IoT_Capstone/assets/136789448/174e5ae8-095d-40a7-99eb-b79fd8b6d3cc)

## 기능 시연
- ### 앱 사용 화면<br/><br/>
![화면 캡처 2024-06-20 135315](https://github.com/Jix0xo/IoT_Capstone/assets/136789448/4a646729-7b08-497c-ab98-89b79ab10c7c)<br/>
![화면 캡처 2024-06-20 135334](https://github.com/Jix0xo/IoT_Capstone/assets/136789448/e301b9ad-e4cb-4744-a3e5-4744f120b36c)
<br/><br/>
![YouTube](https://img.shields.io/badge/YouTube-%23FF0000.svg?style=for-the-badge&logo=YouTube&logoColor=white)<br/><br/>
- [정자세 교정 시연 영상](https://youtu.be/GOmz6sikODg)<br/><br/>
- [옆으로 누운 자세 교정 시연 영상](https://youtu.be/rLDzfJ1hkUs)<br/><br/>
- [코골이 완화 시연 영상](https://youtu.be/LvnRewWps84)<br/><br/>
- [앱 시연 영상](https://youtu.be/gmcnFOtOViI)


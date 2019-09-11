# 0911_CAN 통신 2 - java Setting

### COM 포트를 통한 직렬 통신 - java Setting

1. [New] - [Java project] 

2. [Project] - [Build path] - jar 파일 추가

   > RXTXcomm.jar

3. jre\bin 폴더에 dll 파일 추가

   C:\Program Files\Java\jre1.8.0_211\bin 에 dll 파일 추가

   > rxtxParallel.dll
   >
   > rxtxSerial.dll

4. [Window] - [Preferences] - [Java] - [Installed JREs] - [Edit] 

![1568189401731](C:\Users\student\AppData\Roaming\Typora\typora-user-images\1568189401731.png)

<br>

### 수신 Mask ID

- 00000000

  - 어떤 송신 ID로 보내도 상관 없음!

  - Don't care

![1568178573770](C:\Users\student\AppData\Roaming\Typora\typora-user-images\1568178573770.png)

<bR>

- 마스크 값 지정
  - 마스크 값에 1을 주면 그 자리의 송신 ID와 수신 ID의 비트값이 같아야 데이터를 수신함

![1568178735274](C:\Users\student\AppData\Roaming\Typora\typora-user-images\1568178735274.png)

<BR>

- Unicast 통신

  - 마스크값을 모두 1로 줘서 수신 ID와 송신 ID가 완전히 일치할때만 데이터를 받는 것
  
  ![1568178986782](C:\Users\student\AppData\Roaming\Typora\typora-user-images\1568178986782.png)

<br>

#### => 데이터를 받냐 안 받냐는 수신 노드의 마스크 값이 결정함



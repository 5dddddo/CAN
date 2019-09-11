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

![1568189401731](https://user-images.githubusercontent.com/50972986/64696109-93d8e380-d4d8-11e9-8da8-58841a13b6fe.png)

<br>

### 수신 Mask ID

- 00000000

  - 어떤 송신 ID로 보내도 상관 없음!

  - Don't care

![1568178573770](https://user-images.githubusercontent.com/50972986/64696130-9cc9b500-d4d8-11e9-914b-b9e13fd4eb29.png)

<bR>

- 마스크 값 지정
  - 마스크 값에 1을 주면 그 자리의 송신 ID와 수신 ID의 비트값이 같아야 데이터를 수신함

![1568178735274](https://user-images.githubusercontent.com/50972986/64696143-ab17d100-d4d8-11e9-8b28-1f369824bf3b.png)

<BR>

- Unicast 통신

  - 마스크값을 모두 1로 줘서 수신 ID와 송신 ID가 완전히 일치할때만 데이터를 받는 것
  
  ![1568178986782](https://user-images.githubusercontent.com/50972986/64696136-a521f000-d4d8-11e9-80d9-022d9653f2db.png)

<br>

#### => 데이터를 받냐 안 받냐는 수신 노드의 마스크 값이 결정함



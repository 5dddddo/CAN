# 0911_CAN 통신 3 - 데이터 송수신

#### CANPro Analyzer 프로그램 역할을 java로 구현해보자

> java Setting 관련 내용은 [0911_CAN 통신 2 - Java Setting.md](https://github.com/5dddddo/CAN/blob/master/00_CAN%20%EC%A0%95%EB%A6%AC/0911_CAN%20%ED%86%B5%EC%8B%A0%202%20-%20Java%20Setting.md) 참고
>
> 전체 소스코드 Ex01_DataFrameReceiver.java 참고

<br>

#### 0. 변수 선언

``` JAVA
// 사용할 COM 포트를 지정하기 위해서 필요
private CommPortIdentifier portIdentifier;

// COM 포트가 사용 가능하여
// 해당 포트를 open 하면 COM 포토 객체를 return해 줌
private CommPort commPort;

// COM 포트의 2가지 종류 : Serial, Parallel
// CAN 통신은 Serial 통신을 함
// 따라서, COM 포트의 타입을 알아내서 Type casting 시킴
private SerialPort serialPort;

// Port 객체로부터 Stream을 얻어내서 입출력 할 수 있음
// Reader 계열은 String 단위로 처리함
// byte 단위의 처리하기 위해 BufferedInputStream, OutputStream 사용
private BufferedInputStream bis;
private OutputStream out;
```

<br>

#### 1. 데이터 수신 여부 환경 읽기 및 설정

``` java
String portName = "COM10";

private void connectPort(String portName) {
    try {
        // portName을 이용해 Port에 접근해서 객체를 생성
        // 문자열을 CommPortIdentifier 객체로 바꿔줌
        portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        printMsg(portName + "에 연결을 시도합니다.");

        // isCurrentlyOwned() : Port가 이미 점유되어 사용되고 있는지 확인하는 method
        if (portIdentifier.isCurrentlyOwned()) {
            printMsg(portName + "가 다른 프로그램에 의해서 사용되고 있어요.");
        } else {
            // 포트가 존재하고 사용할 수 있는 상태이면 포트를 열고 포트 객체를 획득
            // 첫번째 인자는 포트를 여는 프로그램의 이름 ( 문자열 )
            // 두번째 인자는 포트를 열 때 기다릴 수 있는 시간 ( ms )
            commPort = portIdentifier.open("MyApp", 5000);
            
            // 포트 객체를 얻은 후 이 포트 객체가 Serial인지 Parallel 인지를
            // 확인한 후 적절하게 type casting
            if (commPort instanceof SerialPort) {
                // Serial Port 객체를 얻어낼 수 있음
                serialPort = (SerialPort) commPort;
                // Serial Port에 대한 설정을 해야 함
                serialPort.setSerialPortParams(
                        // Serial 속도
                        38400,
                        // 데이터의 Bit
                        SerialPort.DATABITS_8,
                        // Stop Bit 설정
                        SerialPort.STOPBITS_1,
                        // Parity Bit는 사용 안 함
                        SerialPort.PARITY_NONE);

                // Serial Port를 Open하고 설정까지 완료한 상황
                // 나에게 들어오는 Data Frame을 받아들일 수 있는 상태
----------------------------------------------------------------------------------
                // Data Frame이 전달 되는 것을 감지하기 위해서 Event 처리 기법을 이용
                // 데이터가 들어오는 걸 감지하고 처리하는 Listener 객체가 있어야 함
                // 이런 Listener 객체를 만들어서
    			// addEventListener()를 이용해 Port에 리스너를 등록해주면 됨
```

<br>

- Listener Class 정의

``` java
// Listener 객체를 만들기 위한 class
// inner class 형식으로 event 처리 listener class를 작성
class MyPortListener implements SerialPortEventListener {
    @Override
    public void serialEvent(SerialPortEvent event) {
        // Serial Port에서 Event가 발생하면 호출
        // port를 통해서 데이터가 들어왔다는 의미
        if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            byte[] readBuffer = new byte[128];
            try {
                // bis.available() > 0이면 데이터가 있다는 의미
                while (bis.available() > 0)
                    // buffer의 사이즈만큼 읽음
                    bis.read(readBuffer);
                String result = new String(readBuffer);
                printMsg("받은 메시지는 " + result);
				...
```

<br>

- SerialPort 객체에 Listener 등록

``` java
                // Listener 객체 정의
                serialPort.addEventListener(new MyPortListener());
                // Port에서 데이터가 들어왔을 때 알림 기능 on
                serialPort.notifyOnDataAvailable(true);
                printMsg(portName + "에 리스너가 등록되었습니다.");

                // 입출력을 하기 위해서 stream을 열어야 함
                bis = new BufferedInputStream(serialPort.getInputStream());
                out = serialPort.getOutputStream();
```

<br>

-  ##### CAN 데이터 수신 여부 환경 읽기 및 설정 
  
  -  현재 CANPro의 CAN 데이터 수신 여부 환경을 읽어오거나 설정할 때 사용하는 명령으로써
    
    설정 시 CANPro 모듈의 내부에서는 CAN 수신 동작을 시작/중지하며
    
    이전에 수신한 CAN 수신 데이터를 지움
  
  - 동작 요청 명령
    - 프로토콜을 이용해서 정해진 형식대로 문자열을 만들어서 out Stream을 통해서 출력
  
    | 시작 문자 | 명령 코드 |                    수신 여부<br>명령 코드                    | Check Sum | 끝 문자 |
    | :-------: | :-------: | :----------------------------------------------------------: | :-------: | :-----: |
    |     :     |     G     | 00 : 현재 수신여부 <br>환경 읽기<br>10 : 수신 중지<br>11 : 수신 시작 | Hex ASCII |   \r    |
    |   1문자   |   1문자   |                            2문자                             |   2문자   |  1문자  |
  
  - Check Sum
  
    - 통신 프로토콜 Frame에서 시작 문자, 끝 문자를 제외한 나머지를 모두 더한 후
  
      0xff로 And 연산한 결과의 1바이트 값에 대응하는 **대문자** Hex ASCII 문자열
  
    - 예 ) 통신 프로토콜 Frame이 :G11A9\r 일 때, Check Sum 구하기
  
      ​	  'G' + '1' + '1' = 0x47 + 0x31 + 0x31 = A9
  
    ```java
    // CAN 장비가 수신을 시작한다는 의미의 문자열
    String msg = ":G11A9\r";
    try {
        byte[] inputData = msg.getBytes();
        out.write(inputData);
        printMsg(portName + "가 수신을 시작합니다.");
    ```

<br>

#### 2. 데이터 송신 데이터 쓰기

- CAN 네트워크상에 특정 CAN Message를 보내고자 할 때 사용하는 명령

- 동작 요청 명령

  | 시작 문자 | 명령 코드 | 송신 데이터<br>특성 코드 | CAN<br>송신 ID | CAN<br/>송신 데이터 | Check Sum | 끝 문자 |
  | :-------: | :-------: | :----------------------: | :------------: | :-----------------: | :-------: | :-----: |
  |     :     |     W     |        Hex ASCII         |   Hex ASCII    |      Hex ASCII      | Hex ASCII |   \r    |
  |   1문자   |   1문자   |          2문자           |   4 or 8문자   |      0~16문자       |   2문자   |  1문자  |

  - 송신 데이터 특성 코드 ( 16진수 2문자 : 76543210 순서)

    - 송신 CAN Message Mode (5번째 비트)
      - 0 : CAN2.0A
      - 1 : CAN2.0B
    - 송신 CAN Message Data 타입 (4번째 비트) 
      - 0 : Data Frame
      - 1 : Remote Frame
    - 송신 CAN Message의 데이터 길이 (3~0번째 비트) : 0 ~ 8 사이의 값을 가짐
      - 8 이면 Hex ASCII 문자 8개

  - CAN 송신 ID

    - 송신 데이터 특성 코드 중

      CAN Message Mode가 0 ( CAN2.0A )이면 4문자

      CAN Message Mode가 0 ( CAN2.0B )이면 8문자

  - CAN 송신 데이터 

    - 송신 데이터 특성 코드 중

    ​	   송신 CAN Message의 데이터 길이에 따라 0 ~ 16문자를 보냄

    <br>

- CAN Message 

  - :W280000000010000000000001234+CheckSum+\r

    - 송신 데이터 특성 코드 : 0x28
      - 2진수 0010 1000 : CAN 2.0B, Data Frame, 데이터 길이는 Hex ASCII 문자 8개

    - CAN 송신 ID
      - 000000001
    - CAN 송신 데이터 
      - 0000000000001234

  - Check Sum 구하는 함수

  ``` java
  public String getCheckSum(){
      String msg = "W28000000010000000000001234";
  
      int result = 0;
      for (char c : msg.toCharArray()) {
          result += (int) c;
      }
      result = result & 0xFF;
      String chk = Integer.toHexString(result).toUpperCase();
      return  ":" + msg + chk + "\r";
  }
  ```

<br>

- OutStream으로 데이터 송신하기

``` java
String res = getCheckSum();
byte[] inputData = res.getBytes();
out.write(inputData);
printMsg(portName + "에서 " + res + "를 전송합니다.");
```


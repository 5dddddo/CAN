# 0911_CAN 통신 3 - java

#### CANPro Analyzer 프로그램 역할을 java로 구현해보자

> java Setting 관련 내용은 0911_CAN 통신 2 - Java Setting.md 참고

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

#### 1. 데이터 수신

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

- Listener Class

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





``` java
    
                // 당연히 Listener 객체를 만들기 위한 class가 있어야 함
                serialPort.addEventListener(new MyPortListener());
                // Port에서 데이터가 들어왔을 때 알림 기능 on
                serialPort.notifyOnDataAvailable(true);
                printMsg(portName + "에 리스너가 등록되었습니다.");
                // 입출력을 하기 위해서 stream을 열어야 함
                bis = new BufferedInputStream(serialPort.getInputStream());
                out = serialPort.getOutputStream();
            }
        }
    }
}
```





#### 2. 데이터 송신

``` java

```


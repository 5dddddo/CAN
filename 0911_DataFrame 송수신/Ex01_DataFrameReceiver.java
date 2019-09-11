package cantest;

import java.awt.Event;
import java.io.BufferedInputStream;
import java.io.OutputStream;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

public class Ex01_DataFrameReceiver extends Application {

	// 메세지 창 : 받은 메세지 출력
	TextArea textarea;

	// 연결 버튼 : COM 포트 연결
	Button connBtn, sendBtn;

	// 사용할 COM 포트를 지정하기 위해서 필요
	private CommPortIdentifier portIdentifier;

	// 만약 COM 포트를 사용할 수 있고 해당포트를 open 하면
	// COM 포토 객체를 획득
	private CommPort commPort;

	// COM 포트의 2가지 종류 : Serial, Parallel
	// CAN 통신은 Serial 통신을 함
	// 따라서, COM 포트의 타입을 알아내서 Type casting 시킴
	private SerialPort serialPort;

	// Port 객체로부터 Stream을 얻어내서 입출력 할 수 있음
	// Reader 계열은 string 타입으로 처리함
	// byte 단위의 처리하기 위해 BufferedInputStream, OutputStream
	private BufferedInputStream bis;
	private OutputStream out;

	// Listener 객체를 만들기 위한 class
	// inner class 형식으로 event 처리 listener class를 작성
	class MyPortListener implements SerialPortEventListener {
		@Override
		public void serialEvent(SerialPortEvent event) {
			// Serial Port에서 Event가 발생하면 호출
			if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {

				// port를 통해서 데이터가 들어왔다는 의미
				byte[] readBuffer = new byte[128];
				try {
					// 데이터가 있으면
					while (bis.available() > 0) {
						// buffer의 사이즈만큼 읽음
						bis.read(readBuffer);
					}
					String result = new String(readBuffer);
					printMsg("받은 메시지는 " + result);
				} catch (Exception e) {
					System.out.println(e);
				}
			}
		}
	}

	private void printMsg(String msg) {
		Platform.runLater(() -> {
			textarea.appendText(msg + "\n");
		});
	}

	private void connectPort(String portName) {
		// portName을 이용해 Port에 접근해서 객체를 생성
		try {
			// 문자열을 CommPortIdentifier 객체로 바꿔줌
			portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
			printMsg(portName + "에 연결을 시도합니다.");

			// isCurrentlyOwned() : Port가 이미 점유되어 사용되고 있는지 확인하는 method
			if (portIdentifier.isCurrentlyOwned()) {
				printMsg(portName + "가 다른 프로그램에 의해서 사용되고 있어요.");
			} else {
				// 포트가 존재하고 사용할 수 있는 상태
				// 포트를 열고 포트 객체를 획득
				// 첫번째 인자는 포트를 여는 프로그램의 이름 ( 문자열 )
				// 두번재 인자는 포트를 열 때 기다릴 수 있는 시간 ( ms )
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
					// Data Frame이 전달 되는 것을 감지하기 위해서 Event 처리 기법을 이용
					// 데이터가 들어오는 걸 감지하고 처리하는 Listener 객체가 있어야 함
					// 이런 Listener 객체를 만들어서 Port에 리스너로 등록해주면 됨
					// 당연히 Listener 객체를 만들기 위한 class가 있어야 함
					serialPort.addEventListener(new MyPortListener());
					// Port에서 데이터가 들어왔을 때 알림 기능 on
					serialPort.notifyOnDataAvailable(true);
					printMsg(portName + "에 리스너가 등록되었습니다.");
					// 입출력을 하기 위해서 stream을 열어야 함
					bis = new BufferedInputStream(serialPort.getInputStream());
					out = serialPort.getOutputStream();

					// CAN 데이터 수신 허용 설정
					// 이 작업은 어떻게 해야 하나?
					// 프로토콜을 이용해서 정해진 형식대로 문자열을 만들어서
					// out Stream을 통해서 출력

					// CAN 장비가 수신을 시작한다는 의미의 문자열
					String msg = ":G11A9\r";
					try {
						byte[] inputData = msg.getBytes();
						out.write(inputData);
						printMsg(portName + "가 수신을 시작합니다.");

					} catch (Exception e) {
						System.out.println(e);
					}

				}
			}
		} catch (Exception e) {
			// 발생한 Exception을 처리하는 코드가 들어와야 함
			System.out.println(e);
		}
	}

	public String getCheckSum() {
		String msg = "W28000000010000000000001234";

		int result = 0;
		for (char c : msg.toCharArray()) {
			result += (int) c;
		}
		result = result & 0xFF;
		String chk = Integer.toHexString(result).toUpperCase();
		return ":" + msg + chk + "\r";
	}

	public void sendMsg(String portName) {
		// portName을 이용해 Port에 접근해서 객체를 생성
		try {
			// 문자열을 CommPortIdentifier 객체로 바꿔줌
			portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
			printMsg(portName + "에 연결을 시도합니다.");

			// isCurrentlyOwned() : Port가 이미 점유되어 사용되고 있는지 확인하는 method
			if (portIdentifier.isCurrentlyOwned()) {
				printMsg(portName + "가 다른 프로그램에 의해서 사용되고 있어요.");
			} else {
				// 포트가 존재하고 사용할 수 있는 상태
				// 포트를 열고 포트 객체를 획득
				// 첫번째 인자는 포트를 여는 프로그램의 이름 ( 문자열 )
				// 두번재 인자는 포트를 열 때 기다릴 수 있는 시간 ( ms )
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
					// Data Frame이 전달 되는 것을 감지하기 위해서 Event 처리 기법을 이용
					// 데이터가 들어오는 걸 감지하고 처리하는 Listener 객체가 있어야 함
					// 이런 Listener 객체를 만들어서 Port에 리스너로 등록해주면 됨
					// 당연히 Listener 객체를 만들기 위한 class가 있어야 함
					serialPort.addEventListener(new MyPortListener());
					// Port에서 데이터가 들어왔을 때 알림 기능 on
					serialPort.notifyOnDataAvailable(true);
					printMsg(portName + "에 리스너가 등록되었습니다.");
					// 입출력을 하기 위해서 stream을 열어야 함
					bis = new BufferedInputStream(serialPort.getInputStream());
					out = serialPort.getOutputStream();

					try {
						String res = getCheckSum();
						byte[] inputData = res.getBytes();
						out.write(inputData);
						printMsg(portName + "에서 " + res + "를 전송합니다.");

					} catch (Exception e) {
						System.out.println(e);
					}
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		BorderPane root = new BorderPane();
		root.setPrefSize(700, 500);
		textarea = new TextArea();
		root.setCenter(textarea);

		connBtn = new Button("COM포트 연결");
		connBtn.setPrefSize(250, 50);
		connBtn.setOnAction(t -> {
			String portName = "COM10";
			// 포트 접속
			connectPort(portName);
		});

		sendBtn = new Button("Send MSG");
		sendBtn.setPrefSize(250, 50);
		sendBtn.setOnAction(t -> {
			String portName = "COM10";
			// 포트 접속
			sendMsg(portName);
		});

		FlowPane flowpane = new FlowPane();
		flowpane.setPrefSize(700, 50);
		// FlowPane에 버튼 올리기
		flowpane.getChildren().add(connBtn);
		flowpane.getChildren().add(sendBtn);
		root.setBottom(flowpane);

		// 실제 Window에 띄우기 위해 Scene 객체 필요
		Scene scene = new Scene(root);
		// Stage primaryStage : 실제 Window 객체
		primaryStage.setScene(scene);
		primaryStage.setTitle("Thread Pool 예제입니다");
		// Window에 띄우기
		primaryStage.show();

	}

	public static void main(String[] args) {
		// launch() : start()를 실행시키는 함수
		launch();
	}

}

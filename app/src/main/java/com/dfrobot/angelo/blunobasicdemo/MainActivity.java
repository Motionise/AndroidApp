package com.dfrobot.angelo.blunobasicdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity  extends BlunoLibrary {
	RelativeLayout parentView;
	ImageView greenbarImgView;
	ViewGroup.LayoutParams params;
	private Button buttonScan, calibration;
	private TextView serialReceivedText, flexTxt, kalmanTxt, magTxt;
	String colon = ":", sensorDatastr;
	String[] sensorData = new String[3];
	String[] flexStr = new String[2];
	String[] kalmanStr = new String[2];
	String[] magStr = new String[4];
	int flex, mx, my, mz, resFlex, greenbarHeight, tempFlex = 0;
	double kalman;
	boolean isSetDefaultSensorData = false;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        onCreateProcess();														//onCreate Process by BlunoLibrary


        serialBegin(115200);													//set the Uart Baudrate on BLE chip to 115200


        serialReceivedText=(TextView) findViewById(R.id.serialReveicedText);	//initial the EditText of the received data			//initial the EditText of the sending data

		flexTxt = (TextView)findViewById(R.id.flexTxt);
		kalmanTxt = (TextView)findViewById(R.id.kalmanTxt);
		magTxt = (TextView)findViewById(R.id.magTxt);

        buttonScan = (Button) findViewById(R.id.buttonScan);					//initial the button for scanning the BLE device
        buttonScan.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				buttonScanOnClickProcess();										//Alert Dialog for selecting the BLE device
			}
		});

        parentView = (RelativeLayout)findViewById(R.id.parentView);
        greenbarImgView = (ImageView)findViewById(R.id.greenBarImgView);
        calibration = (Button) findViewById(R.id.calibration);

        calibration.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				isSetDefaultSensorData = false;
				flex = 0;
				greenbarImgView.setY(parentView.getRootView().getHeight()/3);
			}
		});

		params = (ViewGroup.LayoutParams) greenbarImgView.getLayoutParams();
		greenbarHeight = greenbarImgView.getBottom();
	}

	protected void onResume(){
		super.onResume();
		System.out.println("BlUNOActivity onResume");
		onResumeProcess();														//onResume Process by BlunoLibrary
	}
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		onActivityResultProcess(requestCode, resultCode, data);					//onActivityResult Process by BlunoLibrary
		super.onActivityResult(requestCode, resultCode, data);
	}
	
    @Override
    protected void onPause() {
        super.onPause();
        flexTxt.setText("");
        magTxt.setText("");
        kalmanTxt.setText("");
        onPauseProcess();														//onPause Process by BlunoLibrary
    }
	
	protected void onStop() {
		super.onStop();
		onStopProcess();														//onStop Process by BlunoLibrary
	}
    
	@Override
    protected void onDestroy() {
        super.onDestroy();	
        onDestroyProcess();														//onDestroy Process by BlunoLibrary
    }

	@Override
	public void onConectionStateChange(connectionStateEnum theConnectionState) {//Once connection state changes, this function will be called
		switch (theConnectionState) {											//Four connection state
		case isConnected:
			buttonScan.setText("Connected");
			break;
		case isConnecting:
			buttonScan.setText("Connecting");
			break;
		case isToScan:
			buttonScan.setText("Scan");
			break;
		case isScanning:
			buttonScan.setText("Scanning");
			break;
		case isDisconnecting:
			buttonScan.setText("isDisconnecting");
			break;
		default:
			break;
		}
	}

	@Override
	public void onSerialReceived(String theString) {							//Once connection data received, this function will be called
		// TODO Auto-generated method stub
		// 블루투스 통신 속도가 낮아, 데이터를 충분히 받고 처리 함수에 전달.
		serialReceivedText.append(theString);
		sensorDatastr = serialReceivedText.getText().toString();
		if(sensorDatastr.contains("\n")) {
			sensorDatastr = sensorDatastr.replace("\n", "");
			serialReceivedText.setText("");
			stringProcessing(sensorDatastr);
		}

	}

	public void stringProcessing(String theString) {

		try { // 블루투스 통신 중, 쓰레기 값이 들어오는 경우가 있으므로 예외 처리
			sensorData = theString.split(colon);

			//본격적인 처리부분
			flexStr = sensorData[0].split("\\s");
			kalmanStr = sensorData[1].split("\\s");
			magStr = sensorData[2].split("\\s");
			// string to Int
			// 이 값을 토대로 애니메이션 구현
			flex = Integer.parseInt(flexStr[1]);
			kalman = Double.parseDouble(kalmanStr[1]);
			mx = Integer.parseInt(magStr[1]);
			my = Integer.parseInt(magStr[2]);
			mz = Integer.parseInt(magStr[3]);
			//디버깅 용도
			//System.out.println(flex + " " + kalman + " " + mx + " " + my + " " + mz);

			if(isSetDefaultSensorData == false)
			{
				tempFlex = flex;
				isSetDefaultSensorData = true;
			}
			resFlex = (flex - tempFlex) * 5;
			System.out.println(resFlex + " " + flex + " " + tempFlex);
			greenbarImgView.setRotation((float)(kalman/4)*2);
			System.out.println("greenbar height: " + greenbarHeight);
			greenbarImgView.setPivotY(greenbarImgView.getBottom());
			//greenbarImgView.setLayoutParams(params);
			greenbarImgView.setY(parentView.getRootView().getHeight()/3 - resFlex);

		} catch (Exception e) {
			tempFlex = 0;
			flex = 0;
			resFlex = 0;
			Log.e("sensor", theString);
		}
	}
}
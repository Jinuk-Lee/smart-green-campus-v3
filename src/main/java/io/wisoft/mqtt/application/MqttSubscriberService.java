package io.wisoft.mqtt.application;

import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;

import org.json.JSONObject;


//동기적 동작 방식
@Component
public class MqttSubscriberService implements MqttCallback {

    private MqttClient mqttClient;
    private MqttConnectOptions mqttOptions;
    // LinkedHashMap 객체 생성
    public LinkedHashMap<String, String> mqttDataMap = new LinkedHashMap<>();


    public MqttSubscriberService init(String server, String clientId) throws MqttException {

        mqttOptions = new MqttConnectOptions();
        mqttOptions.setCleanSession(true);
        mqttOptions.setKeepAliveInterval(600000);
        mqttClient = new MqttClient(server, clientId);
        mqttClient.setCallback(this);
        mqttClient.connect(mqttOptions);

        return this;
    }

    // 커넥션이 종료되면 호출 - 통신 오류로 연결이 끊어지는 경우 호출
    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("연결이 중단되었습니다.");
    }

    // 메시지가 도착하면 호출
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        handleMessage(topic, String.valueOf(message));
        //데이터를 JSON 형식으로 변환하여 출력
        JSONObject jsonObject = new JSONObject(mqttDataMap);
        String jsonOutput = jsonObject.toString();
        System.out.println(jsonOutput);
        System.out.println(jsonOutput.getBytes());

        // API 호출 코드 추가
        String apiUrl = "http://api.example.com/endpoint";
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        // 필요한 경우 헤더 설정
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(jsonOutput.getBytes());
        outputStream.flush();
        outputStream.close();


        // API 호출
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            System.out.println("API Response: " + response.toString());
        } else {
            System.out.println("API request failed with response code: " + responseCode);
        }
    }




    // Mosquitto 구독을 통해 데이터를 받아서 맵에 저장하는 메소드
    public void handleMessage(String topic, String message) {
        // 메시지를 맵에 추가
        mqttDataMap.put(topic, message);
    }


    // 구독 신청
    public boolean subscribe(String topic) throws MqttException {

        if (topic != null) {
            mqttClient.subscribe(topic, 0);
        }

        return true;
    }

    // 메시지의 배달이 완료되면 호출
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }
}

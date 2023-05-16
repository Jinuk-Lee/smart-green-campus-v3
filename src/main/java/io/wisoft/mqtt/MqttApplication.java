package io.wisoft.mqtt;

import io.wisoft.mqtt.application.MqttSubscriberService;
import jakarta.annotation.PostConstruct;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@Configuration
public class MqttApplication {

    @Value("${mqtt.server}")
    private String server;

    @Value("${mqtt.client-id}")
    private String clientId;

    @Value("${mqtt.topic}")
    private String topic;

    //해당 토픽을 구독
    @PostConstruct
    public void subscribe() throws MqttException {
        mqttSubscriberService().subscribe(topic);
    }

    //구독후 server와 clientId를 수신
    public MqttSubscriberService mqttSubscriberService() throws MqttException {
        return new MqttSubscriberService()
                .init(server, clientId);
    }

    public static void main(String[] args) {
        SpringApplication.run(MqttApplication.class, args);
    }
}

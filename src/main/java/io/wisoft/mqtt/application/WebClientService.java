package io.wisoft.mqtt.application;

import io.wisoft.mqtt.config.MqttConfig;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Service
public class WebClientService {

    MqttConfig mqttConfig = new MqttConfig();

    public void requestToApiServer(final MqttMessage message) {

        Map<String, Object> bodyMap = extractdata(message);

        final WebClient webClient = WebClient
                .builder()
                .baseUrl(mqttConfig.baseUrl)
                .defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .build();

        final Map<String, Object> response = (Map<String, Object>) webClient
                .post()
                .uri(mqttConfig.apiUrl)
                .bodyValue(bodyMap)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    public static void post(final MqttMessage message) {
        final WebClientService webClientService = new WebClientService();
        webClientService.requestToApiServer(message);
    }

    private Map<String, Object> extractdata(final MqttMessage message) {

        final Map<String, Object> bodyMap = new HashMap<>();

        final String str = new String(message.getPayload());
        final StringTokenizer st = new StringTokenizer(str, "=,");

        st.nextToken();
        final Long id = Long.valueOf(st.nextToken());

        st.nextToken();
        final Double number = Double.valueOf(st.nextToken());

        st.nextToken();
        final String measurement = st.nextToken();

        bodyMap.put("memberId", id);
        bodyMap.put("value", number);
        bodyMap.put("measurement", measurement);

        return bodyMap;
    }
}
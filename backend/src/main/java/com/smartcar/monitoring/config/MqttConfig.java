package com.smartcar.monitoring.config;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class MqttConfig {

    @Value("${mqtt.broker.url}")
    private String brokerUrl;

    @Value("${mqtt.client.id}")
    private String clientId;

    @Value("${mqtt.username}")
    private String username;

    @Value("${mqtt.password}")
    private String password;

    @Value("${mqtt.connection.timeout:30}")
    private int connectionTimeout;

    @Value("${mqtt.keep.alive.interval:60}")
    private int keepAliveInterval;

    @Value("${mqtt.clean.session:true}")
    private boolean cleanSession;

    @Value("${mqtt.auto.reconnect:true}")
    private boolean autoReconnect;

    @Bean
    public MqttClient mqttClient() throws MqttException {
        String finalClientId = clientId;
        if (clientId.contains("${random.uuid}")) {
            finalClientId = "smart-car-backend-" + UUID.randomUUID().toString().substring(0, 8);
        }

        MqttClient mqttClient = new MqttClient(brokerUrl, finalClientId, new MemoryPersistence());

        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setConnectionTimeout(connectionTimeout);
        options.setKeepAliveInterval(keepAliveInterval);
        options.setCleanSession(cleanSession);
        options.setAutomaticReconnect(autoReconnect);

        mqttClient.connect(options);
        return mqttClient;
    }
}

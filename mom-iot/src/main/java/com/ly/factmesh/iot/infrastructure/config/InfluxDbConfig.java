package com.ly.factmesh.iot.infrastructure.config;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * InfluxDB 时序数据库配置
 *
 * @author LY-FactMesh
 */
@Configuration
public class InfluxDbConfig {

    @Value("${influxdb.url:http://localhost:8086}")
    private String url;

    @Value("${influxdb.token:ly-factmesh-iot-token}")
    private String token;

    @Value("${influxdb.org:ly-factmesh}")
    private String org;

    @Value("${influxdb.bucket:iot-telemetry}")
    private String bucket;

    @Bean
    public InfluxDBClient influxDBClient() {
        return InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket);
    }

    @Bean
    public String influxBucket() {
        return bucket;
    }
}

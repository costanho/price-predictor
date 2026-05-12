package com.pricemonitor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "modelserver")
public class ModelServerConfig {

	private String url;
	private String forecastEndpoint;
	private String anomalyDetectionEndpoint;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getForecastEndpoint() {
		return forecastEndpoint;
	}

	public void setForecastEndpoint(String forecastEndpoint) {
		this.forecastEndpoint = forecastEndpoint;
	}

	public String getAnomalyDetectionEndpoint() {
		return anomalyDetectionEndpoint;
	}

	public void setAnomalyDetectionEndpoint(String anomalyDetectionEndpoint) {
		this.anomalyDetectionEndpoint = anomalyDetectionEndpoint;
	}
}

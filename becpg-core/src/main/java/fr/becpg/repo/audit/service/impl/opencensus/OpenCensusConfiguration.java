package fr.becpg.repo.audit.service.impl.opencensus;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import io.opencensus.exporter.trace.stackdriver.StackdriverTraceConfiguration;
import io.opencensus.exporter.trace.stackdriver.StackdriverTraceExporter;
import io.opencensus.tags.TagKey;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.config.TraceParams;
import io.opencensus.trace.samplers.Samplers;

@Service
public class OpenCensusConfiguration {

	public static final TagKey HOST_KEY = TagKey.create("becpg/host");
	public static final TagKey INSTANCE_KEY = TagKey.create("becpg/instance");

	public static final double SEARCH_SAMPLING_PROBABILITY = 1d / 50d;
	public static final double REMOTE_CHECK_SAMPLING_PROBABILITY = 1d / 100d;
	public static final double REMOTE_GET_SAMPLING_PROBABILITY = 1d / 100d;

	@PostConstruct
	public void init() throws IOException {
		String gcpProjectId = System.getenv("GOOGLE_CLOUD_PROJECT");

		if (gcpProjectId != null) {

			String host = System.getProperty("alfresco.host");
			String instance = System.getProperty("becpg.instance.name");

			if ((instance == null) || instance.isEmpty()) {
				instance = "OnPremise";
			}
			
			if ((host == null) || host.isEmpty()) {
				host = "localhost";
			}

			Map<String, AttributeValue> fixedAttributes = new HashMap<>();

			fixedAttributes.put("becpg/host", AttributeValue.stringAttributeValue(host));
			fixedAttributes.put("becpg/instance", AttributeValue.stringAttributeValue(instance));

			StackdriverTraceExporter.createAndRegister(
					StackdriverTraceConfiguration.builder().setFixedAttributes(fixedAttributes).setProjectId(gcpProjectId).build());

		}

		TraceConfig traceConfig = Tracing.getTraceConfig();
		TraceParams activeTraceParams = traceConfig.getActiveTraceParams();
		traceConfig.updateActiveTraceParams(activeTraceParams.toBuilder().setSampler(Samplers.alwaysSample()).build());

	}

}
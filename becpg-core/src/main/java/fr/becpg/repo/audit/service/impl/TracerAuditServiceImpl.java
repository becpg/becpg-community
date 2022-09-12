package fr.becpg.repo.audit.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.repo.audit.service.TracerAuditService;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;

@Service
public class TracerAuditServiceImpl implements TracerAuditService {

	private static final Tracer tracer = Tracing.getTracer();
	
	private static final Log logger = LogFactory.getLog(TracerAuditServiceImpl.class);

	@Override
	public AutoCloseable start(String tracerScopeName) {
		return tracer.spanBuilder(tracerScopeName).startScopedSpan();
	}

	@Override
	public void stop(AutoCloseable scope) {
		try {
			scope.close();
		} catch (Exception e) {
			logger.error("Error while closing scope : " + e.getMessage());
		}
	}

	@Override
	public void putAttribute(String string, Object attribute) {
		if (attribute != null) {
			tracer.getCurrentSpan().putAttribute(string, AttributeValue.stringAttributeValue(attribute.toString()));
		}
	}

	@Override
	public void addAnnotation(String string) {
		tracer.getCurrentSpan().addAnnotation(string);
	}

}

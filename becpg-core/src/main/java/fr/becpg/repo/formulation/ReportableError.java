package fr.becpg.repo.formulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

public class ReportableError {

    public enum ReportableErrorType {
        WARNING, ERROR
    }

    private ReportableErrorType type;
    private String message;
    private MLText displayMessage;
    private List<NodeRef> sources = new ArrayList<>();

	public ReportableError(ReportableErrorType type, String message, MLText displayMessage, List<NodeRef> sources) {
		this.type = type;
		this.message = message;
		this.displayMessage = displayMessage;
		if (sources != null) {
			this.sources.addAll(sources);
		}
	}
	
	public ReportableErrorType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public MLText getDisplayMessage() {
        return displayMessage;
    }

    public List<NodeRef> getSources() {
        return sources;
    }

	@Override
	public String toString() {
		return "ReportableError [type=" + type + ", message=" + message + ", displayMessage=" + displayMessage
				+ ", sources=" + sources + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(displayMessage, message, sources, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReportableError other = (ReportableError) obj;
		return Objects.equals(displayMessage, other.displayMessage)
				&& Objects.equals(message, other.message) && Objects.equals(sources, other.sources)
				&& type == other.type;
	}

}

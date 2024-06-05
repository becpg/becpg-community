package fr.becpg.repo.formulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * <p>ReportableError class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ReportableError {

    public enum ReportableErrorType {
        WARNING, ERROR
    }

    private ReportableErrorType type;
    private String message;
    private MLText displayMessage;
    private List<NodeRef> sources = new ArrayList<>();

	/**
	 * <p>Constructor for ReportableError.</p>
	 *
	 * @param type a {@link fr.becpg.repo.formulation.ReportableError.ReportableErrorType} object
	 * @param message a {@link java.lang.String} object
	 * @param displayMessage a {@link org.alfresco.service.cmr.repository.MLText} object
	 * @param sources a {@link java.util.List} object
	 */
	public ReportableError(ReportableErrorType type, String message, MLText displayMessage, List<NodeRef> sources) {
		this.type = type;
		this.message = message;
		this.displayMessage = displayMessage;
		if (sources != null) {
			this.sources.addAll(sources);
		}
	}
	
	/**
	 * <p>Getter for the field <code>type</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.formulation.ReportableError.ReportableErrorType} object
	 */
	public ReportableErrorType getType() {
        return type;
    }

    /**
     * <p>Getter for the field <code>message</code>.</p>
     *
     * @return a {@link java.lang.String} object
     */
    public String getMessage() {
        return message;
    }

    /**
     * <p>Getter for the field <code>displayMessage</code>.</p>
     *
     * @return a {@link org.alfresco.service.cmr.repository.MLText} object
     */
    public MLText getDisplayMessage() {
        return displayMessage;
    }

    /**
     * <p>Getter for the field <code>sources</code>.</p>
     *
     * @return a {@link java.util.List} object
     */
    public List<NodeRef> getSources() {
        return sources;
    }

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "ReportableError [type=" + type + ", message=" + message + ", displayMessage=" + displayMessage
				+ ", sources=" + sources + "]";
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return Objects.hash(displayMessage, message, sources, type);
	}

	/** {@inheritDoc} */
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

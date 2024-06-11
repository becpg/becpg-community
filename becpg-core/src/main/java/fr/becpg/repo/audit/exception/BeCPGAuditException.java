package fr.becpg.repo.audit.exception;

/**
 * <p>BeCPGAuditException class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class BeCPGAuditException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * <p>Constructor for BeCPGAuditException.</p>
	 *
	 * @param msgId a {@link java.lang.String} object
	 */
	public BeCPGAuditException(String msgId) {
		super(msgId);
	}

}

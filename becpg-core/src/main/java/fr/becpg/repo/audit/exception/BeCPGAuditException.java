package fr.becpg.repo.audit.exception;

public class BeCPGAuditException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BeCPGAuditException(String msgId) {
		super(msgId);
	}

}

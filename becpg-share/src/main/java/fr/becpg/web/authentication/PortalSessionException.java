package fr.becpg.web.authentication;

/**
 * <p>PortalSessionException class.</p>
 *
 * A refusal by the portal Share-session endpoint. It carries two messages on purpose:
 *
 * <ul>
 *   <li>{@link #getErrorCode()} is the ONLY thing the caller ever sees — a coarse OAuth 2.0
 *       error code, identical for every reason a token can be bad, so the endpoint cannot be
 *       used as an oracle;</li>
 *   <li>{@link #getMessage()} is the operator-facing reason and goes to the log only. It must
 *       never contain the token, the shared secret, or any part of either.</li>
 * </ul>
 *
 * @author matthieu
 * @since 26.1.0.42
 */
public class PortalSessionException extends Exception {

	private static final long serialVersionUID = 1L;

	/** Constant <code>ERROR_INVALID_REQUEST="invalid_request"</code> */
	public static final String ERROR_INVALID_REQUEST = "invalid_request";

	/** Constant <code>ERROR_INVALID_TOKEN="invalid_token"</code> */
	public static final String ERROR_INVALID_TOKEN = "invalid_token";

	/** Constant <code>ERROR_UNAUTHORIZED_CLIENT="unauthorized_client"</code> */
	public static final String ERROR_UNAUTHORIZED_CLIENT = "unauthorized_client";

	/** Constant <code>ERROR_RATE_LIMITED="rate_limited"</code> */
	public static final String ERROR_RATE_LIMITED = "rate_limited";

	/** Constant <code>ERROR_REPOSITORY_UNAVAILABLE="repository_unavailable"</code> */
	public static final String ERROR_REPOSITORY_UNAVAILABLE = "repository_unavailable";

	/** Constant <code>ERROR_IDENTITY_PROVIDER_UNAVAILABLE="identity_provider_unavailable"</code> */
	public static final String ERROR_IDENTITY_PROVIDER_UNAVAILABLE = "identity_provider_unavailable";

	private final int status;

	private final String errorCode;

	/**
	 * <p>Constructor for PortalSessionException.</p>
	 *
	 * @param status the HTTP status to answer with
	 * @param errorCode the OAuth 2.0 error code handed to the caller
	 * @param reason the operator facing reason, logged and never returned
	 */
	public PortalSessionException(int status, String errorCode, String reason) {
		super(reason);
		this.status = status;
		this.errorCode = errorCode;
	}

	/**
	 * <p>Constructor for PortalSessionException.</p>
	 *
	 * @param status the HTTP status to answer with
	 * @param errorCode the OAuth 2.0 error code handed to the caller
	 * @param reason the operator facing reason, logged and never returned
	 * @param cause a {@link java.lang.Throwable} object
	 */
	public PortalSessionException(int status, String errorCode, String reason, Throwable cause) {
		super(reason, cause);
		this.status = status;
		this.errorCode = errorCode;
	}

	/**
	 * <p>Getter for the field <code>status</code>.</p>
	 *
	 * @return a int
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * <p>Getter for the field <code>errorCode</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getErrorCode() {
		return errorCode;
	}

}

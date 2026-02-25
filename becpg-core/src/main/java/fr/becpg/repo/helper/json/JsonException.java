package fr.becpg.repo.helper.json;

/**
 * <p>JsonException class.</p>
 *
 * @author matthieu
 */
public class JsonException extends RuntimeException {
	
	/**
	 * generated serial number
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * <p>Constructor for JsonException.</p>
	 */
	public JsonException() {
		super();
	}
	
	/**
	 * <p>Constructor for JsonException.</p>
	 *
	 * @param cause a {@link java.lang.Throwable} object.
	 */
	public JsonException(Throwable cause) {
        super(cause);
    }
	
	/**
	 * <p>Constructor for JsonException.</p>
	 *
	 * @param message a {@link java.lang.String} object.
	 */
	public JsonException(String message) {
		super(message);
	}

	/**
	 * <p>Constructor for JsonException.</p>
	 *
	 * @param message a {@link java.lang.String} object.
	 * @param cause a {@link java.lang.Throwable} object.
	 */
	public JsonException(String message, Throwable cause) {
		super(message, cause);
	}

}

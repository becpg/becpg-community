package fr.becpg.repo.authentication;

import fr.becpg.common.BeCPGException;

/**
 * Exception thrown when attempting to create a user that already exists.
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class UserAlreadyExistsException extends BeCPGException {

    /**
     * generated serial number
     */
    private static final long serialVersionUID = 923417999410572526L;

    /**
     * Instantiates a new user already exists exception.
     *
     * @param message the message
     */
    public UserAlreadyExistsException(String message) {
        super(message);
    }

    /**
     * <p>Constructor for UserAlreadyExistsException.</p>
     *
     * @param message a {@link java.lang.String} object.
     * @param cause a {@link java.lang.Throwable} object.
     */
    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * <p>Constructor for UserAlreadyExistsException.</p>
     *
     * @param cause a {@link java.lang.Throwable} object.
     */
    public UserAlreadyExistsException(Throwable cause) {
        super(cause);
    }
}

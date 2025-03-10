package fr.becpg.repo.entity.remote.extractor;

/**
 * <p>RemoteException class.</p>
 *
 * @author matthieu
 */
public class RemoteException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * <p>Constructor for RemoteException.</p>
	 */
	public RemoteException() {
		
	}
	
	/**
	 * <p>Constructor for RemoteException.</p>
	 *
	 * @param message a {@link java.lang.String} object
	 */
	public RemoteException(String message) {
		super(message);
	}

	/**
	 * <p>Constructor for RemoteException.</p>
	 *
	 * @param message a {@link java.lang.String} object
	 * @param cause a {@link java.lang.Exception} object
	 */
	public RemoteException(String message, Exception cause) {
		super(message, cause);
	}
	
}

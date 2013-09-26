package fr.becpg.repo.security;

public class BeCPGAccessDeniedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -56874971164233072L;
	
	public BeCPGAccessDeniedException(String methodQName) {
		super(methodQName);
	}

	

}

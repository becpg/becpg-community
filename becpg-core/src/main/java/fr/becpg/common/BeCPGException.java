/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.common;


/**
 * beCPG Exception.
 *
 * @author querephi
 * @version $Id: $Id
 */
public class BeCPGException extends RuntimeException {
	
	/**
	 * generated serial number
	 */
	private static final long serialVersionUID = 923417999410572525L;

	/**
	 * Instantiates a new beCPG exception.
	 *
	 * @param message the message
	 */
	public BeCPGException(String message) {
		super(message);
	}

	/**
	 * <p>Constructor for BeCPGException.</p>
	 */
	public BeCPGException() {
		super();
	}

	/**
	 * <p>Constructor for BeCPGException.</p>
	 *
	 * @param message a {@link java.lang.String} object.
	 * @param cause a {@link java.lang.Throwable} object.
	 */
	public BeCPGException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * <p>Constructor for BeCPGException.</p>
	 *
	 * @param cause a {@link java.lang.Throwable} object.
	 */
	public BeCPGException(Throwable cause) {
		super(cause);
	}

	
	


}

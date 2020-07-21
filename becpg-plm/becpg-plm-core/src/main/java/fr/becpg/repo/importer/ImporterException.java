/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.importer;

/**
 * Importer Exception.
 *
 * @author querephi
 * @version $Id: $Id
 */
public class ImporterException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7777533275769615822L;

	/**
	 * Instantiates a new importer exception.
	 *
	 * @param message the message
	 */
	public ImporterException(String message) {
		super(message);
	}

	/**
	 * <p>Constructor for ImporterException.</p>
	 *
	 * @param message a {@link java.lang.String} object.
	 * @param e a {@link java.lang.Exception} object.
	 */
	public ImporterException(String message, Exception e) {
		super(message,e);
	}


}

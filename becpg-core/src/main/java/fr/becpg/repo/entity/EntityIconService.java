package fr.becpg.repo.entity;

import java.io.OutputStream;

/**
 * Entity icon service
 *
 * @author gaspard
 */
public interface EntityIconService {

	/**
	 * <p>writeIconCSS.</p>
	 *
	 * @param out a {@link java.io.OutputStream} object
	 */
	public void writeIconCSS(OutputStream out);
}

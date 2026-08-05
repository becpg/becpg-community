/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.template;

import fr.becpg.common.BeCPGException;

/**
 * <p>Raised when a template cannot be resolved or fails while being rendered.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class TemplateRenderException extends BeCPGException {

	private static final long serialVersionUID = -4471985023918365291L;

	/**
	 * <p>Constructor for TemplateRenderException.</p>
	 *
	 * @param message a {@link java.lang.String} object
	 */
	public TemplateRenderException(String message) {
		super(message);
	}

	/**
	 * <p>Constructor for TemplateRenderException.</p>
	 *
	 * @param message a {@link java.lang.String} object
	 * @param cause a {@link java.lang.Throwable} object
	 */
	public TemplateRenderException(String message, Throwable cause) {
		super(message, cause);
	}

}

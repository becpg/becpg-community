/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
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
package fr.becpg.repo.product.formulation.labeling;

import java.util.List;
import java.util.Objects;

import fr.becpg.repo.product.data.constraints.DeclarationType;

/**
 * <p>DeclarationFilter class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class DeclarationFilterRule extends AbstractFormulaFilterRule {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7819866735412692528L;

	private final DeclarationType declarationType;

	private Double threshold = null;

	/**
	 * <p>Constructor for DeclarationFilter.</p>
	 *
	 * @param ruleName a {@link java.lang.String} object.
	 * @param formula a {@link java.lang.String} object.
	 * @param declarationType a {@link fr.becpg.repo.product.data.constraints.DeclarationType} object.
	 * @param locales a {@link java.util.List} object.
	 */
	public DeclarationFilterRule(String ruleName, String formula, DeclarationType declarationType, List<String> locales) {
		super(ruleName, formula, locales);

		this.declarationType = declarationType;

	}

	/**
	 * <p>isThreshold.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isThreshold() {
		return threshold != null;
	}

	/**
	 * <p>Setter for the field <code>threshold</code>.</p>
	 *
	 * @param threshold a {@link java.lang.Double} object.
	 */
	public void setThreshold(Double threshold) {
		this.threshold = threshold;
	}

	/**
	 * <p>Getter for the field <code>threshold</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getThreshold() {
		return threshold;
	}

	/**
	 * <p>Getter for the field <code>declarationType</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.constraints.DeclarationType} object.
	 */
	public DeclarationType getDeclarationType() {
		return declarationType;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(declarationType, threshold);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		DeclarationFilterRule other = (DeclarationFilterRule) obj;
		return declarationType == other.declarationType && Objects.equals(threshold, other.threshold);
	}
	
	

}

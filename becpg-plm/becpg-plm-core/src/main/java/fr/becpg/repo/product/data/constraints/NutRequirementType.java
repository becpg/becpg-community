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
package fr.becpg.repo.product.data.constraints;

/**
 * <p>NutListRequirementType class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public enum NutRequirementType {

	/*
	 * Important don't change enum order, requirement will be sort by this order
	 */
	Serving, GdaPerc, AsPrepared;

	/**
	 * <p>fromString.</p>
	 *
	 * @param type a {@link java.lang.String} object.
	 * @return a {@link fr.becpg.repo.product.data.constraints.NutListRequirementType} object.
	 */
	public static NutRequirementType fromString(String type) {
		try {
			return NutRequirementType.valueOf(type);
		} catch (IllegalArgumentException | NullPointerException e) {
			return null;
		}
	}
}
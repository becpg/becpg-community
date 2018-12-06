/*******************************************************************************
 * Copyright (C) 2010-2018 beCPG.
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

public enum TareUnit {
	g, kg, lb, oz;

	/**
	 * Convert factor to Kg
	 * 
	 * @return
	 */
	public Double getUnitFactor() {
		if (this.equals(g)) {
			return 1000d;
		}
		if (this.equals(lb)) {
			return 1 / 0.4536d;
		} else if (this.equals(oz)) {
			return 1 / 0.02835d;
		}
		return 1d;
	}

}

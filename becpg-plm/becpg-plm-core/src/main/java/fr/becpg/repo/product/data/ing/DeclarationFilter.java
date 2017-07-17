/*******************************************************************************
 * Copyright (C) 2010-2017 beCPG. 
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
package fr.becpg.repo.product.data.ing;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.constraints.DeclarationType;

/**
 * 
 * @author matthieu
 *
 */
public class DeclarationFilter {

	private final String formula;
	
	private final DeclarationType declarationType;

	private final Set<Locale> locales = new HashSet<>();
	
	private Double threshold = null;
	

	public DeclarationFilter(String formula, DeclarationType declarationType, List<String> locales) {
		super();
		this.formula = formula;
		this.declarationType = declarationType;
		if(locales!=null){
			for (String tmp : locales) {	
				this.locales.add(MLTextHelper.parseLocale(tmp));
			}
		}
	}
	
	public boolean isThreshold(){
		return threshold!=null;
	}
	
	
	public void setThreshold(Double threshold) {
		this.threshold = threshold;
	}
	

	public Double getThreshold() {
		return threshold;
	}

	public String getFormula() {
		return formula;
	}


	public DeclarationType getDeclarationType() {
		return declarationType;
	}


	public boolean matchLocale(Locale locale) {
		return locales.isEmpty() || locales.contains(locale);
	}

	
}

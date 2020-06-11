/*******************************************************************************
 * Copyright (C) 2010-2020 beCPG. 
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

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.alfresco.service.cmr.repository.MLText;

import fr.becpg.repo.helper.MLTextHelper;

/**
 * 
 * @author matthieu
 *
 */
public class SeparatorRule {

	private final Set<Locale> locales = new HashSet<>();
	
	private Double threshold = null;

	private MLText mlText;

	public SeparatorRule( MLText mlText, Double threshold, List<String> locales) {
		super();
		this.threshold = threshold;
		this.mlText = mlText;
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

	public String getClosestValue(Locale locale) {
		String ret = null;

		if ((ret == null) || ret.isEmpty()) {
			ret = MLTextHelper.getClosestValue(mlText, locale);
		}

		return ret;
	}


	public boolean matchLocale(Locale locale) {
		return locales.isEmpty() || locales.contains(locale);
	}

	
}

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

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.alfresco.service.cmr.repository.MLText;

import fr.becpg.repo.helper.MLTextHelper;

/**
 * <p>SeparatorRule class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class SeparatorRule {

	private final Set<Locale> locales = new HashSet<>();
	
	private Double threshold = null;

	private MLText mlText;

	/**
	 * <p>Constructor for SeparatorRule.</p>
	 *
	 * @param mlText a {@link org.alfresco.service.cmr.repository.MLText} object.
	 * @param threshold a {@link java.lang.Double} object.
	 * @param locales a {@link java.util.List} object.
	 */
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
	
	/**
	 * <p>isThreshold.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isThreshold(){
		return threshold!=null;
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
	 * <p>getClosestValue.</p>
	 *
	 * @param locale a {@link java.util.Locale} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String getClosestValue(Locale locale) {
		return MLTextHelper.getClosestValue(mlText, locale);
	}


	/**
	 * <p>matchLocale.</p>
	 *
	 * @param locale a {@link java.util.Locale} object.
	 * @return a boolean.
	 */
	public boolean matchLocale(Locale locale) {
		return locales.isEmpty() || locales.contains(locale);
	}

	
}

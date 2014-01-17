/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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
package fr.becpg.config.format;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.repo.RepoConsts;

/**
 * Format of properties (used for import and reports)
 * @author querephi
 *
 */
public class PropertyFormats {

	private static final String FORMAT_DECIMAL_VALUE = "0.####";
	
	private boolean useDefaultLocale = true;
	
	protected DateFormat dateFormat;
	
	protected DateFormat datetimeFormat;
	
	protected DecimalFormat decimalFormat;

	
	public boolean isUseDefaultLocale() {
		return useDefaultLocale;
	}

	public void setUseDefaultLocale(boolean useDefaultLocale) {
		this.useDefaultLocale = useDefaultLocale;
	}

	public DateFormat getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(DateFormat dateFormat) {
		this.dateFormat = dateFormat;
	}

	public DateFormat getDatetimeFormat() {
		return datetimeFormat;
	}

	public void setDatetimeFormat(DateFormat datetimeFormat) {
		this.datetimeFormat = datetimeFormat;
	}

	public DecimalFormat getDecimalFormat() {
		return decimalFormat;
	}

	public void setDecimalFormat(DecimalFormat decimalFormat) {
		this.decimalFormat = decimalFormat;
	}
	
	public PropertyFormats(boolean useDefaultLocal){
		
		this.useDefaultLocale = useDefaultLocal;
		
		if(useDefaultLocal){
		
			dateFormat = new SimpleDateFormat(RepoConsts.FORMAT_DATE, Locale.getDefault());
			datetimeFormat = new SimpleDateFormat(RepoConsts.FORMAT_DATETIME, Locale.getDefault());
			
		}
		else{
		
			dateFormat = new SimpleDateFormat(RepoConsts.FORMAT_DATE,I18NUtil.getLocale());
			datetimeFormat = new SimpleDateFormat(RepoConsts.FORMAT_DATETIME,I18NUtil.getLocale());
		}
		
		decimalFormat = new DecimalFormat(FORMAT_DECIMAL_VALUE);
	}

}

/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
	
    private  ThreadLocal<SimpleDateFormat> s_localDateFormat = new ThreadLocal<SimpleDateFormat>(){
    	protected SimpleDateFormat initialValue() {
    		if(useDefaultLocale){
    			return new SimpleDateFormat(dateFormat,Locale.getDefault());
    		} else {
    			return new SimpleDateFormat(dateFormat, I18NUtil.getLocale());
    		}
    	};
    	
    };
    
    private  ThreadLocal<SimpleDateFormat> s_localDateTimeFormat = new ThreadLocal<SimpleDateFormat>(){
    	protected SimpleDateFormat initialValue() {
    		if(useDefaultLocale){
    			return new SimpleDateFormat(datetimeFormat,Locale.getDefault());
    		} else {
    			return new SimpleDateFormat(datetimeFormat, I18NUtil.getLocale());
    		}
    	};
    	
    };
    
    private  ThreadLocal<DecimalFormat> s_localDecimalFormat = new ThreadLocal<DecimalFormat>(){
    	protected DecimalFormat initialValue() {
    		return new DecimalFormat(decimalFormat);
    	};
    	
    };
	
	private boolean useDefaultLocale = true;
	
	protected String dateFormat;
	
	protected String datetimeFormat;
	
	protected String decimalFormat;

	
	public boolean isUseDefaultLocale() {
		return useDefaultLocale;
	}

	public void setUseDefaultLocale(boolean useDefaultLocale) {
		s_localDateFormat.remove();
		s_localDateTimeFormat.remove();
		this.useDefaultLocale = useDefaultLocale;
	}

	
	public void setDateFormat(String dateFormat) {
		s_localDateFormat.remove();
		this.dateFormat = dateFormat;
	}

	public void setDatetimeFormat(String datetimeFormat) {
		s_localDateTimeFormat.remove();
		this.datetimeFormat = datetimeFormat;
	}

	public void setDecimalFormat(String decimalFormat) {
		s_localDecimalFormat.remove();
		this.decimalFormat = decimalFormat;
	}

	public PropertyFormats(boolean useDefaultLocal){
		
		this.useDefaultLocale = useDefaultLocal;
		
		dateFormat = RepoConsts.FORMAT_DATE;
		datetimeFormat = RepoConsts.FORMAT_DATETIME;
		decimalFormat = FORMAT_DECIMAL_VALUE;
		
	
	}
	
	public String formatDate(Object o) {
		return s_localDateFormat.get().format(o);
	}

	public String formatDecimal(Object o) {
		return s_localDecimalFormat.get().format(o);
	}

	public String formatDateTime(Object o) {
		return s_localDateTimeFormat.get().format(o);
	}

	public Date parseDate(String dateString) throws ParseException {
		return s_localDateFormat.get().parse(dateString);
	}

	public Number parseDecimal(String decimalString) throws ParseException {
		return s_localDecimalFormat.get().parse(decimalString);
	}

	public DecimalFormat getDecimalFormat() {
		return s_localDecimalFormat.get();
	}


}

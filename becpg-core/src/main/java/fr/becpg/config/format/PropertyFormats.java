package fr.becpg.config.format;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import fr.becpg.common.RepoConsts;

/**
 * Format of properties (used for import and reports)
 * @author querephi
 *
 */
public class PropertyFormats {

	private boolean useDefaultLocale = true;
	
	private DateFormat dateFormat;
	
	private DateFormat datetimeFormat;
	
	private DecimalFormat decimalFormat;

	
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
		
			dateFormat = new SimpleDateFormat(RepoConsts.FORMAT_DATE);
			datetimeFormat = new SimpleDateFormat(RepoConsts.FORMAT_DATETIME);
		}
	}
}

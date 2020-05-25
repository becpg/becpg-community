package fr.becpg.config.format;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

@Service
public class PropertyFormatService implements InitializingBean {

//	private String dateFormat = "EEE d MMM yyyy";	
//	private String dateTimeFormat = "EEE d MMM yyyy HH:mm:ss";
//	private String decimalFormat = "###,###.####";
//	
//	
// --> Export Search
//	<settings>
//		<setting id="dateFormat" value="yyyy-MM-dd HH:mm:ss" />
//		<setting id="datetimeFormat" value="yyyy-MM-dd HH:mm:ss" />
//		<!-- <setting id="decimalPattern" value="#0.00"/> -->
//	</settings>
	

//Import : 
//	private static final String FORMAT_DATE_FRENCH = "dd/MM/yyyy";
//	private static final String FORMAT_DATE_ENGLISH = "yyyy/MM/dd";
//
//	propertyFormats = new PropertyFormats(true);
//	
//	String dateFormat = (Locale.getDefault().equals(Locale.FRENCH) || Locale.getDefault().equals(Locale.FRANCE)) ? FORMAT_DATE_FRENCH
//			: FORMAT_DATE_ENGLISH;
//	propertyFormats.setDateFormat(dateFormat);
//	
	public static final String PROCESS_DATETIME_FORMAT = "dd MMMM, yyyy";
	
	
	private final PropertyFormats csvPropertyFormats = new CSVPropertyFormats(false);

	private final PropertyFormats propertyFormats = new PropertyFormats(false);
	
	

	public PropertyFormats getPropertyFormats(FormatMode mode, boolean useServerLocale) {
		PropertyFormats ret;
		
		if(FormatMode.PROCESS.equals(mode) || useServerLocale ) {
			ret =  new PropertyFormats(useServerLocale);
		} else {
			ret =  FormatMode.CSV.equals(mode) ? csvPropertyFormats : propertyFormats;
		}
		if(FormatMode.PROCESS.equals(mode)) {
			ret.setDateFormat(PROCESS_DATETIME_FORMAT);
		}
		return ret;
		
	}
	private static PropertyFormatService INSTANCE;

	@Override
	public void afterPropertiesSet() throws Exception {
		INSTANCE = this;

	}

	public static PropertyFormatService getInstance() {
		return INSTANCE;
	}

	
	
	
	

	
}

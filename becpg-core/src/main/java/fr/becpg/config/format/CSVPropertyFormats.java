package fr.becpg.config.format;

import java.text.SimpleDateFormat;
import java.util.Locale;

import org.springframework.extensions.surf.util.I18NUtil;

public class CSVPropertyFormats extends PropertyFormats {

	public CSVPropertyFormats(boolean useDefaultLocal) {
		super(useDefaultLocal);

		if (Locale.FRENCH.equals(I18NUtil.getContentLocaleLang())) {
			dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH);
			datetimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.FRENCH);
		} else {
			dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
			datetimeFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.ENGLISH);
		}
	}

}

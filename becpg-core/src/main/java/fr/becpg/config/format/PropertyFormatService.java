package fr.becpg.config.format;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

/**
 * <p>PropertyFormatService class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class PropertyFormatService {

	/** Constant <code>PROCESS_DATETIME_FORMAT="dd MMMM, yyyy"</code> */
	public static final String PROCESS_DATETIME_FORMAT = "dd MMMM, yyyy";

	Map<String, PropertyFormats> cache = new HashMap<>();

	/**
	 * <p>Getter for the field <code>propertyFormats</code>.</p>
	 *
	 * @param mode a {@link fr.becpg.config.format.FormatMode} object.
	 * @param useServerLocale a boolean.
	 * @return a {@link fr.becpg.config.format.PropertyFormats} object.
	 */
	public PropertyFormats getPropertyFormats(final FormatMode mode, boolean useServerLocale) {

		return cache.computeIfAbsent((mode != null ? mode.toString() + "-" : "") + useServerLocale, k -> {
			PropertyFormats ret;

			if (FormatMode.PROCESS.equals(mode) || useServerLocale) {
				ret = new PropertyFormats(useServerLocale);
			} else {
				ret = FormatMode.CSV.equals(mode) ? new CSVPropertyFormats(false) : new PropertyFormats(false);
			}
			if (FormatMode.PROCESS.equals(mode)) {
				ret.setDateFormat(PROCESS_DATETIME_FORMAT);
			}
			return ret;

		});

	}

}

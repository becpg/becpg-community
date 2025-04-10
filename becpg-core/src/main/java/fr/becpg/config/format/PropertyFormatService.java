package fr.becpg.config.format;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

/**
 * <p>PropertyFormatService class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class PropertyFormatService implements InitializingBean {

	/** Constant <code>PROCESS_DATETIME_FORMAT="dd MMMM, yyyy"</code> */
	public static final String PROCESS_DATETIME_FORMAT = "dd MMMM, yyyy";

	private static PropertyFormatService instance = null;

	Map<String, PropertyFormats> cache = new ConcurrentHashMap<>();

	/** {@inheritDoc} */
	@Override
	public void afterPropertiesSet() throws Exception {
		instance = this;
	}

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

	/**
	 * <p>instance.</p>
	 *
	 * @return a {@link fr.becpg.config.format.PropertyFormatService} object
	 */
	public static PropertyFormatService instance() {
		return instance;
	}

}

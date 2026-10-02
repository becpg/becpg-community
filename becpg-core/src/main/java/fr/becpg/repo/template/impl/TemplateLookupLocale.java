/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG.
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
package fr.becpg.repo.template.impl;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * <p>Reduces a locale to the part FreeMarker may safely append to a template name.</p>
 *
 * <p>With localized lookup on, FreeMarker builds the name of the template to load from
 * {@link Locale#toString()}. A locale parsed from an <code>Accept-Language</code> header can carry
 * an arbitrary variant such as <code>/../../x</code>, which turns the lookup into a path traversal
 * (CVE-2026-84939, fixed in FreeMarker 2.3.35). Only a well-formed language and country are kept:
 * no shipped template is localized by script or variant.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public final class TemplateLookupLocale {

	private static final Pattern LANGUAGE_PATTERN = Pattern.compile("[a-z]{2,8}");

	private static final Pattern COUNTRY_PATTERN = Pattern.compile("[A-Z]{2}|[0-9]{3}");

	private TemplateLookupLocale() {
	}

	/**
	 * <p>Returns the locale to hand to FreeMarker for a localized lookup.</p>
	 *
	 * @param locale the requested locale, may be <code>null</code>
	 * @return the language and country of the locale when they are well-formed, the language alone
	 *         when only the country is malformed, {@link Locale#ROOT} otherwise
	 */
	public static Locale sanitize(Locale locale) {
		if ((locale == null) || !LANGUAGE_PATTERN.matcher(locale.getLanguage()).matches()) {
			return Locale.ROOT;
		}
		if (!COUNTRY_PATTERN.matcher(locale.getCountry()).matches()) {
			return Locale.of(locale.getLanguage());
		}
		return Locale.of(locale.getLanguage(), locale.getCountry());
	}

}

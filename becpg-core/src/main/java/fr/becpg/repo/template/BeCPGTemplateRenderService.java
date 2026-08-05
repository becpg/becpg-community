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
package fr.becpg.repo.template;

import java.io.Writer;
import java.util.Locale;
import java.util.Map;

/**
 * <p>Renders FreeMarker templates that are looked up in the repository first, then on the
 * classpath, so that a customer can override a shipped template without redeploying the module.</p>
 *
 * <p>Template names are flat, extension included (for instance {@code nutritionFacts-vertical.ftl}).
 * The locale is honoured through the standard FreeMarker localized lookup, which tries
 * {@code name_fr_FR.ftl}, then {@code name_fr.ftl}, then {@code name.ftl}. The same rule applies to
 * the templates pulled in by {@code &lt;#import&gt;}, so a shared macro library must stay
 * locale-neutral.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface BeCPGTemplateRenderService {

	/**
	 * <p>Renders a template and returns its output.</p>
	 *
	 * @param templateName a {@link java.lang.String} object
	 * @param locale a {@link java.util.Locale} object, the current locale is used when null
	 * @param model a {@link java.util.Map} object
	 * @return a {@link java.lang.String} object
	 * @throws fr.becpg.repo.template.TemplateRenderException if the template is missing or fails
	 */
	String render(String templateName, Locale locale, Map<String, Object> model);

	/**
	 * <p>Renders a template directly into the given writer, for large outputs.</p>
	 *
	 * @param templateName a {@link java.lang.String} object
	 * @param locale a {@link java.util.Locale} object, the current locale is used when null
	 * @param model a {@link java.util.Map} object
	 * @param writer a {@link java.io.Writer} object
	 * @throws fr.becpg.repo.template.TemplateRenderException if the template is missing or fails
	 */
	void render(String templateName, Locale locale, Map<String, Object> model, Writer writer);

	/**
	 * <p>Tells whether a template resolves, in the repository or on the classpath.</p>
	 *
	 * @param templateName a {@link java.lang.String} object
	 * @param locale a {@link java.util.Locale} object, the current locale is used when null
	 * @return a boolean
	 */
	boolean exists(String templateName, Locale locale);

	/**
	 * <p>Drops the template cache, so that a template just updated in the repository is picked up
	 * without waiting for the update delay.</p>
	 */
	void clearCache();

}

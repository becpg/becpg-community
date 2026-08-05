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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.template.BeCPGTemplateRenderService;
import fr.becpg.repo.template.TemplateRenderException;
import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.core.TemplateClassResolver;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateNotFoundException;

/**
 * <p>FreeMarker implementation of {@link fr.becpg.repo.template.BeCPGTemplateRenderService},
 * backed by a private {@link freemarker.template.Configuration}.</p>
 *
 * <p>A private configuration is used rather than the Alfresco {@code TemplateService} because the
 * latter resolves templates by repository path only: a template read from the repository could not
 * pull in a shared macro library, which is exactly what a family of templates needs. It also lets
 * the number format be pinned to {@code computer}, without which a coordinate such as {@code 144.5}
 * would be written {@code 144,5} in a French locale and would silently corrupt any XML output.</p>
 *
 * <p>Templates using the {@code .ftlx} extension are auto-escaped as XML, which is what SVG output
 * requires; plain {@code .ftl} templates stay unescaped.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class FreemarkerTemplateRenderServiceImpl implements BeCPGTemplateRenderService, InitializingBean {

	private static final Log logger = LogFactory.getLog(FreemarkerTemplateRenderServiceImpl.class);

	/** Number format keeping a dot as decimal separator, whatever the locale. */
	private static final String COMPUTER_NUMBER_FORMAT = "computer";

	/** Delay before a template updated in the repository is reloaded. */
	private static final long TEMPLATE_UPDATE_DELAY_MS = 60000L;

	private NodeService nodeService;

	private ContentService contentService;

	private RepoService repoService;

	private String repositoryFolderPath;

	private String classpathPrefix;

	private Configuration configuration;

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>contentService</code>.</p>
	 *
	 * @param contentService a {@link org.alfresco.service.cmr.repository.ContentService} object
	 */
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	/**
	 * <p>Setter for the field <code>repoService</code>.</p>
	 *
	 * @param repoService a {@link fr.becpg.repo.helper.RepoService} object
	 */
	public void setRepoService(RepoService repoService) {
		this.repoService = repoService;
	}

	/**
	 * <p>Setter for the field <code>repositoryFolderPath</code>.</p>
	 *
	 * @param repositoryFolderPath a {@link java.lang.String} object, folder holding the overrides
	 */
	public void setRepositoryFolderPath(String repositoryFolderPath) {
		this.repositoryFolderPath = repositoryFolderPath;
	}

	/**
	 * <p>Setter for the field <code>classpathPrefix</code>.</p>
	 *
	 * @param classpathPrefix a {@link java.lang.String} object, classpath root of the templates
	 */
	public void setClasspathPrefix(String classpathPrefix) {
		this.classpathPrefix = classpathPrefix;
	}

	/** {@inheritDoc} */
	@Override
	public void afterPropertiesSet() {
		configuration = buildConfiguration();
	}

	private Configuration buildConfiguration() {
		Configuration config = new Configuration(Configuration.VERSION_2_3_30);
		config.setTemplateLoader(buildTemplateLoader());
		config.setDefaultEncoding(StandardCharsets.UTF_8.name());
		config.setNumberFormat(COMPUTER_NUMBER_FORMAT);
		config.setRecognizeStandardFileExtensions(true);
		config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		config.setLogTemplateExceptions(false);
		config.setWrapUncheckedExceptions(true);
		config.setLocalizedLookup(true);
		config.setTemplateUpdateDelayMilliseconds(TEMPLATE_UPDATE_DELAY_MS);
		// A template is rendered with the rights of the formulation, and templates are editable
		// content: denying ?new keeps a template from instantiating arbitrary classes.
		config.setNewBuiltinClassResolver(TemplateClassResolver.ALLOWS_NOTHING_RESOLVER);
		return config;
	}

	/**
	 * Repository first so that a customer override wins, classpath second for the shipped
	 * templates. Stickiness must stay off, otherwise a template freshly added to the repository
	 * would keep being served from the classpath until the cache expires.
	 */
	private TemplateLoader buildTemplateLoader() {
		MultiTemplateLoader templateLoader = new MultiTemplateLoader(new TemplateLoader[] {
				new RepositoryTemplateLoader(nodeService, contentService, repoService, repositoryFolderPath),
				new ClassTemplateLoader(FreemarkerTemplateRenderServiceImpl.class, classpathPrefix) });
		templateLoader.setSticky(false);
		return templateLoader;
	}

	/** {@inheritDoc} */
	@Override
	public String render(String templateName, Locale locale, Map<String, Object> model) {
		StringWriter writer = new StringWriter();
		render(templateName, locale, model, writer);
		return writer.toString();
	}

	/** {@inheritDoc} */
	@Override
	public void render(String templateName, Locale locale, Map<String, Object> model, Writer writer) {
		if (logger.isDebugEnabled()) {
			logger.debug("Rendering template " + templateName + " for locale " + locale);
		}

		try {
			getTemplate(templateName, locale).process(model, writer);
		} catch (TemplateNotFoundException e) {
			throw new TemplateRenderException("Template not found: " + templateName, e);
		} catch (IOException | TemplateException e) {
			throw new TemplateRenderException("Cannot render template: " + templateName, e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean exists(String templateName, Locale locale) {
		try {
			getTemplate(templateName, locale);
			return true;
		} catch (IOException e) {
			logger.debug("Template not found: " + templateName, e);
			return false;
		}
	}

	/** {@inheritDoc} */
	@Override
	public void clearCache() {
		configuration.clearTemplateCache();
	}

	private Template getTemplate(String templateName, Locale locale) throws IOException {
		return configuration.getTemplate(templateName, locale != null ? locale : I18NUtil.getLocale());
	}

}

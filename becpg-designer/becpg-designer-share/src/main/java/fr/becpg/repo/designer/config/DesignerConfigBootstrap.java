/*******************************************************************************
 * Copyright (C) 2010-2020 beCPG. 
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
package fr.becpg.repo.designer.config;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.extensions.config.ConfigDeployer;
import org.springframework.extensions.config.ConfigDeployment;
import org.springframework.extensions.config.ConfigException;
import org.springframework.extensions.config.ConfigService;
import org.springframework.extensions.config.source.UrlConfigSource;

/**
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>" Patched
 *         ConfigBootstrap to used DesignerUrlConfigSource
 */
public class DesignerConfigBootstrap implements BeanNameAware, ConfigDeployer {

	private static final String PREFIX_FILE = "file:";

	private static final String WILDCARD = "*";

	private static final Log logger = LogFactory.getLog(DesignerConfigBootstrap.class);

	/** The bean name. */
	private String beanName;

	protected ConfigService configService;
	protected List<String> configs;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang
	 * .String)
	 */
	public void setBeanName(String name) {
		this.beanName = name;
	}

	/**
	 * Set the configs
	 * 
	 * @param configs
	 *            the configs
	 */
	public void setConfigs(List<String> configs) {
		this.configs = configs;
	}

	/**
	 * Sets the ConfigService instance to deploy to
	 * 
	 * @param configService
	 *            ConfigService instance to deploy to
	 */
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	/**
	 * Method called by ConfigService when the configuration files represented
	 * by this ConfigDeployer need to be initialised.
	 * 
	 * @return List of ConfigDeployment objects
	 */
	public List<ConfigDeployment> initConfig() {
		List<ConfigDeployment> deployed = null;

		if (configService != null && this.configs != null && !this.configs.isEmpty()) {
			List<String> configsToAdd = processWildcards(this.configs);

			if (configsToAdd != null && !configsToAdd.isEmpty()) {
				UrlConfigSource configSource = new UrlConfigSource(configsToAdd, true);
				deployed = configService.appendConfig(configSource);
			}
		}

		return deployed;
	}

	private List<String> processWildcards(List<String> configs) {
		List<String> ret = new ArrayList<>();
		for (String config : configs) {
			ret.addAll(processWidlCards(config));
		}

		return ret;
	}

	// file:${dir.root}/designer/*.xml
	// file:C:\Alfresco\alf_data\*.xml

	private List<String> processWidlCards(String sourceString) {
		List<String> ret = new ArrayList<>();
		if (sourceString != null && sourceString.startsWith(PREFIX_FILE) && sourceString.contains(WILDCARD)) {
			char separator = guessSeparator(sourceString);
			logger.debug("processWildCards: " + sourceString);
			File dir = new File(sourceString.substring(PREFIX_FILE.length(), sourceString.lastIndexOf(separator)));
			if (dir != null && dir.exists()) {
				FileFilter fileFilter = new WildcardFileFilter(sourceString.substring(sourceString.lastIndexOf(separator) + 1));
				File[] files = dir.listFiles(fileFilter);
				if (files != null) {
					for (File file : files) {
						logger.debug("Add config file : " + PREFIX_FILE + file.getAbsolutePath());
						ret.add(PREFIX_FILE + file.getAbsolutePath());
					}
				}
			}
		} else {
			logger.debug("Add config file : " + sourceString);
			ret.add(sourceString);
		}
		return ret;
	}

	private char guessSeparator(String sourceString) {
		return sourceString.lastIndexOf('\\') > -1 ? '\\' : '/';
	}

	/**
	 * Registers this object with the injected ConfigService
	 */
	public void register() {
		if (configService == null) {
			throw new ConfigException("Config service must be provided");
		}

		configService.addDeployer(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.config.ConfigDeployer#getSortKey()
	 */
	public String getSortKey() {
		return this.beanName;
	}
}

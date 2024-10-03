/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
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
 * You should have received a copy of the GNU Lesser General Public License along with beCPG.
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.report.search.impl;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import fr.becpg.config.mapping.AttributeMapping;
import fr.becpg.config.mapping.CharacteristicMapping;
import fr.becpg.config.mapping.FileMapping;
import fr.becpg.repo.report.entity.EntityReportData;

/**
 * <p>
 * ReportServerSearchContext class.
 * </p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ReportServerSearchContext {


	private List<AttributeMapping> attributeColumns = new ArrayList<>();

	private List<CharacteristicMapping> characteristicsColumns = new ArrayList<>();

	private List<FileMapping> fileColumns = new ArrayList<>();

	private EntityReportData reportData = new EntityReportData();

	private Element nodesElt;
	private Element filesElt;

	/** Constant <code>TAG_EXPORT="export"</code> */
	public static final String TAG_EXPORT = "export";
	/** Constant <code>TAG_NODES="nodes"</code> */
	public static final String TAG_NODES = "nodes";
	/** Constant <code>TAG_FILES="files"</code> */
	public static final String TAG_FILES = "files";
	/** Constant <code>TAG_NODE="node"</code> */
	public static final String TAG_NODE = "node";
	/** Constant <code>TAG_FILE="file"</code> */
	public static final String TAG_FILE = "file";
	/** Constant <code>ATTR_ID="id"</code> */
	public static final String ATTR_ID = "id";

	/** Constant <code>TAG_SITE="siteId"</code> */
	public static final String TAG_SITE = "siteId";

	/**
	 * <p>Constructor for ReportServerSearchContext.</p>
	 */
	public ReportServerSearchContext() {
		super();

		Document document = DocumentHelper.createDocument();
		Element exportElt = document.addElement(TAG_EXPORT);
		nodesElt = exportElt.addElement(TAG_NODES);
		filesElt = exportElt.addElement(TAG_FILES);

		reportData.setXmlDataSource(exportElt);
	}

	/**
	 * <p>Getter for the field <code>reportData</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.report.entity.EntityReportData} object
	 */
	public EntityReportData getReportData() {
		return reportData;
	}

	/**
	 * <p>createFileElt.</p>
	 *
	 * @return a {@link org.dom4j.Element} object
	 */
	public Element createFileElt() {
		return filesElt.addElement(TAG_FILE);
	}

	/**
	 * <p>createNodeElt.</p>
	 *
	 * @param idx a {@link java.lang.Long} object
	 * @return a {@link org.dom4j.Element} object
	 */
	public Element createNodeElt(Long idx) {
		Element nodeElt = nodesElt.addElement(TAG_NODE);
		nodeElt.addAttribute(ATTR_ID, idx.toString());
		return nodeElt;
	}

	/**
	 * <p>
	 * Getter for the field <code>attributeColumns</code>.
	 * </p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<AttributeMapping> getAttributeColumns() {
		return attributeColumns;
	}

	/**
	 * <p>
	 * Setter for the field <code>attributeColumns</code>.
	 * </p>
	 *
	 * @param attributeColumns
	 *            a {@link java.util.List} object.
	 */
	public void setAttributeColumns(List<AttributeMapping> attributeColumns) {
		this.attributeColumns = attributeColumns;
	}

	/**
	 * <p>
	 * Getter for the field <code>characteristicsColumns</code>.
	 * </p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<CharacteristicMapping> getCharacteristicsColumns() {
		return characteristicsColumns;
	}

	/**
	 * <p>
	 * Setter for the field <code>characteristicsColumns</code>.
	 * </p>
	 *
	 * @param characteristicsColumns
	 *            a {@link java.util.List} object.
	 */
	public void setCharacteristicsColumns(List<CharacteristicMapping> characteristicsColumns) {
		this.characteristicsColumns = characteristicsColumns;
	}

	/**
	 * <p>
	 * Getter for the field <code>fileColumns</code>.
	 * </p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<FileMapping> getFileColumns() {
		return fileColumns;
	}

	/**
	 * <p>
	 * Setter for the field <code>fileColumns</code>.
	 * </p>
	 *
	 * @param fileColumns
	 *            a {@link java.util.List} object.
	 */
	public void setFileColumns(List<FileMapping> fileColumns) {
		this.fileColumns = fileColumns;
	}

}

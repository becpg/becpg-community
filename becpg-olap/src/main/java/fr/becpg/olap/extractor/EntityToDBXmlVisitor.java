/*******************************************************************************
 * Copyright (C) 2010-2017 beCPG. 
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
package fr.becpg.olap.extractor;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import fr.becpg.tools.InstanceManager.Instance;
import fr.becpg.tools.jdbc.JdbcUtils;

/**
 * 
 * @author matthieu
 * 
 */
public class EntityToDBXmlVisitor {

	private static final String ATTR_TYPE = "type";
	private static final String ATTR_NAME = "name";
	private static final String ATTR_NODEREF = "nodeRef";

	private final Connection connection;

	private final Instance instance;

	public EntityToDBXmlVisitor(Connection connection, Instance instance) {
		super();
		this.instance = instance;
		this.connection = connection;
	}

	class Column {
		final String key;
		String nodeRef = null;
		final Serializable value;

		public Column(String key, Serializable value) {
			super();
			this.key = key;
			this.value = value;
		}

		public Column(String key, String nodeRef, Serializable value) {
			this.key = key;
			this.value = value;
			this.nodeRef = nodeRef;

		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((key == null) ? 0 : key.hashCode());
			result = prime * result + ((nodeRef == null) ? 0 : nodeRef.hashCode());
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Column other = (Column) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (key == null) {
				if (other.key != null)
					return false;
			} else if (!key.equals(other.key))
				return false;
			if (nodeRef == null) {
				if (other.nodeRef != null)
					return false;
			} else if (!nodeRef.equals(other.nodeRef))
				return false;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}

		private EntityToDBXmlVisitor getOuterType() {
			return EntityToDBXmlVisitor.this;
		}

		@Override
		public String toString() {
			return "Column [key=" + key + ", nodeRef=" + nodeRef + ", value=" + value + "]";
		}
	}

	private static final Log logger = LogFactory.getLog(EntityToDBXmlVisitor.class);

	private static final List<String> ignoredProperties = new ArrayList<>();
	
	private static final List<String> ignoredListProperties = new ArrayList<>();

	private static final List<String> ignoredLists = new ArrayList<>();

	private static final List<String> ignoredTypes = new ArrayList<>();

	static {
		ignoredTypes.add("ecm:changeOrder");
		ignoredTypes.add("sec:aclGroup");
		ignoredTypes.add("bcpg:productSpecification");
		ignoredTypes.add("bcpg:systemEntity");
		ignoredTypes.add("bcpg:productMicrobioCriteria");
	}

	static {
		ignoredListProperties.add("cm:creator");
		ignoredListProperties.add("cm:created");
		ignoredListProperties.add("cm:modified");
		ignoredListProperties.add("cm:modifier");
		ignoredListProperties.add("metadata:siteName");
		ignoredListProperties.add("metadata:siteId");
		ignoredListProperties.add("bcpg:startEffectivity");
		ignoredListProperties.add("bcpg:endEffectivity");
	}
	
	static {
		ignoredProperties.add("cm:name");
		ignoredProperties.add("cm:owner");
		ignoredProperties.add("cm:autoVersionOnUpdateProps");
		ignoredProperties.add("cm:description");
		ignoredProperties.add("cm:autoVersion");
		ignoredProperties.add("cm:title");
		ignoredProperties.add("cm:initialVersion");
		ignoredProperties.add("bcpg:entityLists");
		ignoredProperties.add("cm:contains");
		ignoredProperties.add("bcpg:sort");
		ignoredProperties.add("rep:reports");
		ignoredProperties.add("bcpg:isManualListItem");
		ignoredProperties.add("bcpg:depthLevel");
		ignoredProperties.add("fm:commentCount");
		ignoredProperties.add("bcpg:rclDataType");
		ignoredProperties.add("bcpg:allergenListDecisionTree");
		ignoredProperties.add("bcpg:nutListFormulaErrorLog");
		ignoredProperties.add("bcpg:nutListIsFormulated");
		ignoredProperties.add("bcpg:lclIsFormulated");
		ignoredProperties.add("bcpg:ingListQtyMaxi");
		ignoredProperties.add("bcpg:ingListQtyMini");
		
	}

	static {
		ignoredLists.add("mpm:resourceParamList");
		ignoredLists.add("ecm:replacementList");
		ignoredLists.add("mpm:processList");
		ignoredLists.add("bcpg:dynamicCharactList");
		ignoredLists.add("ecm:changeUnitList");
		ignoredLists.add("ecm:calculatedCharactList");
		ignoredLists.add("ecm:wUsedList");
		ignoredLists.add("pack:labelingList");
		ignoredLists.add("bcpg:costList");
		ignoredLists.add("bcpg:priceList");
		ignoredLists.add("pjt:deliverableList");
		ignoredLists.add("bcpg:labelingRuleList");
		ignoredLists.add("bcpg:contactList");
		ignoredLists.add("bcpg:organoList");
		ignoredLists.add("bcpg:microbioList");
		ignoredLists.add("bcpg:physicoChemList");
		ignoredLists.add("bcpg:ingLabelingList");
		ignoredLists.add("bcpg:activityList");
	}

	public void visit(InputStream in) throws IOException, SAXException, ParserConfigurationException, DOMException, ParseException, SQLException {

		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true); // never forget this!
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		Document doc = builder.parse(in);

		Element entity = (Element) doc.getFirstChild();

		String nodeRef = entity.getAttribute(ATTR_NODEREF);
		String name = entity.getAttribute(ATTR_NAME);
		String type = entity.getNodeName();

		if (!ignoredTypes.contains(type)) {
			Long dbId = createDBEntity(nodeRef, type, name, readProperties(entity));

			NodeList dataLists = entity.getElementsByTagName("dl:dataList");
			for (int i = 0; i < dataLists.getLength(); i++) {
				Element dataList = ((Element) dataLists.item(i));
				String dataListname = dataList.getAttribute(ATTR_NAME);

				NodeList contains = dataList.getElementsByTagName("cm:contains");
				Element container = (Element) contains.item(0);

				NodeList dataListItems = container.getChildNodes();
				for (int j = 0; j < dataListItems.getLength(); j++) {
					Element dataListItem = ((Element) dataListItems.item(j));
					String dataListItemNodeRef = dataListItem.getAttribute(ATTR_NODEREF);

					if (!ignoredLists.contains(dataListItem.getNodeName())) {
						createDBDataListItem(dbId, dataListItemNodeRef, dataListname, dataListItem.getNodeName(), readProperties(dataListItem));
					}
				}

			}
		}

	}

	private Long createDBDataListItem(Long entityId, String dataListItemNodeRef, String dataListname, String itemType, List<Column> properties)
			throws SQLException {
		if (logger.isDebugEnabled()) {
			logger.debug("Create or update datalist item : ");
			logger.debug(" - NodeRef : " + dataListItemNodeRef);
			logger.debug(" - Entity : " + entityId);
			logger.debug(" - DataList name : " + dataListname);
		}

		Long columnId = JdbcUtils.update(connection,
				"insert into `becpg_datalist` "
						+ "(`datalist_id`,`entity_fact_id`,`datalist_name`,`item_type`,`instance_id`,`batch_id`,`is_last_version`) "
						+ " values (?,?,?,?,?,?,?)",
				new Object[] { dataListItemNodeRef, entityId, dataListname, itemType, instance.getId(), instance.getBatchId(), true });

		for (Column column : properties) {
			logger.debug(" --  Property :" + column.toString());
			if (column.value != null && !ignoredListProperties.contains(column.key)) {

				JdbcUtils.update(connection,
						"insert into `becpg_property` " + "(`datalist_id`,`prop_name`,`prop_id`,`" + getColumnTypeName(column.value)
								+ "`,`batch_id`) " + " values (?,?,?,?,?)",
						new Object[] { columnId, column.key, column.nodeRef, extract(column.value), instance.getBatchId() });
			}
		}
		return columnId;

	}

	private Object extract(Serializable value) {
		if (value instanceof Date) {
			Calendar cal = Calendar.getInstance();
			cal.setTime((Date) value);
			return cal.get(Calendar.YEAR) * 10000 + (cal.get(Calendar.MONTH) + 1) * 100 + cal.get(Calendar.DAY_OF_MONTH);
		}
		return value;
	}

	private String getColumnTypeName(Serializable value) {
		return value.getClass().getSimpleName().toLowerCase() + "_value";
	}

	private Long createDBEntity(String nodeRef, String type, String name, List<Column> properties) throws SQLException {
		if (logger.isDebugEnabled()) {
			logger.debug("Create or update entity : ");
			logger.debug(" - NodeRef : " + nodeRef);
			logger.debug(" - Type : " + type);
			logger.debug(" - Name : " + name);
		}

		JdbcUtils.update(connection, "update  `becpg_entity` set is_last_version = ? where  entity_id = ? and instance_id = ?",
				new Object[] { false, nodeRef, instance.getId() });
		
		JdbcUtils.update(connection, "delete from  `becpg_datalist` where  entity_fact_id in (select id from becpg_entity where entity_id = ? and instance_id = ? )  ",
				new Object[] { nodeRef, instance.getId() });

		Long columnId = JdbcUtils.update(connection, "insert into `becpg_entity` "
				+ "(`entity_id`,`entity_type`,`entity_name`,`instance_id`,`batch_id`,`is_last_version`) " + " values (?,?,?,?,?,?)",
				new Object[] { nodeRef, type, name, instance.getId(), instance.getBatchId(), true });

		for (Column column : properties) {
			logger.debug(" --  Property :" + column.toString());
			if (column.value != null) {
				JdbcUtils.update(connection,
						"insert into `becpg_property` " + "(`entity_id`,`prop_name`,`prop_id`,`" + getColumnTypeName(column.value) + "`,`batch_id`) "
								+ " values (?,?,?,?,?)",
						new Object[] { columnId, column.key, column.nodeRef, extract(column.value), instance.getBatchId() });
			}
		}
		return columnId;
	}

	private List<Column> readProperties(Element entity) throws DOMException, ParseException {
		List<Column> ret = new ArrayList<>();

		NodeList properties = entity.getChildNodes();
		for (int j = 0; j < properties.getLength(); j++) {
			if (properties.item(j) instanceof Element) {
				Element property = ((Element) properties.item(j));
				if (!ignoredProperties.contains(property.getNodeName())) {
					String type = property.getAttribute(ATTR_TYPE);
					switch (type) {
					case "d:text":
					case "d:mltext":
					case "d:qname":
						if (property.getTextContent() != null) {
							ret.add(new Column(property.getNodeName(), property.getTextContent()));
						}
						break;
					case "d:datetime":
					case "d:date":
						if (property.getTextContent() != null) {
							ret.add(new Column(property.getNodeName(), parse(property.getTextContent())));
						}
						break;
					case "d:double":
						if (property.getTextContent() != null) {
							Double val = Double.parseDouble(property.getTextContent());
							if (Double.isNaN(val) || Double.isInfinite(val)) {
								val = null;
							}
							ret.add(new Column(property.getNodeName(), val));
						}
						break;
					case "d:float":
						if (property.getTextContent() != null) {
							Float val = Float.parseFloat(property.getTextContent());
							if (Float.isNaN(val) || Float.isInfinite(val)) {
								val = null;
							}
							ret.add(new Column(property.getNodeName(), val));
						}
						break;
					case "d:int":
					case "d:long":
						if (property.getTextContent() != null) {
							ret.add(new Column(property.getNodeName(), Long.parseLong(property.getTextContent())));
						}
						break;
					case "d:noderef":
					case "assoc":
						NodeList propertiesAssoc = property.getChildNodes();
						for (int i = 0; i < propertiesAssoc.getLength(); i++) {
							if (propertiesAssoc.item(i) instanceof Element) {
								Element assoc = ((Element) propertiesAssoc.item(i));
								if (!assoc.getAttribute(ATTR_NODEREF).isEmpty() || !assoc.getAttribute(ATTR_NAME).isEmpty()) {
									ret.add(new Column(property.getNodeName(), assoc.getAttribute(ATTR_NODEREF), assoc.getAttribute(ATTR_NAME)));
								}
							}
						}
						break;
					case "d:boolean":
						if (property.getTextContent() != null) {
							ret.add(new Column(property.getNodeName(), Boolean.parseBoolean(property.getTextContent())));
						}

						break;

					default:
						break;
					}

				}
			} else {
				logger.error("Cannot read property " + properties.item(j).getTextContent());
				return new ArrayList<>();
			}
		}

		return ret;
	}

	private static final ThreadLocal<Map<String, TimeZone>> timezones;
	static {
		timezones = new ThreadLocal<>();
	}

	/**
	 * Parse date from ISO formatted string
	 * 
	 * @param isoDate
	 *            ISO string to parse
	 * @return the date
	 * @throws PlatformRuntimeException
	 *             if the parse failed
	 */
	public static Date parse(String isoDate) {
		Date parsed;

		int offset = 0;

		// extract year
		int year = Integer.parseInt(isoDate.substring(offset, offset += 4));
		if (isoDate.charAt(offset) != '-') {
			throw new IndexOutOfBoundsException("Expected - character but found " + isoDate.charAt(offset));
		}

		// extract month
		int month = Integer.parseInt(isoDate.substring(offset += 1, offset += 2));
		if (isoDate.charAt(offset) != '-') {
			throw new IndexOutOfBoundsException("Expected - character but found " + isoDate.charAt(offset));
		}

		// extract day
		int day = Integer.parseInt(isoDate.substring(offset += 1, offset += 2));
		if (isoDate.charAt(offset) != 'T') {
			throw new IndexOutOfBoundsException("Expected T character but found " + isoDate.charAt(offset));
		}

		// extract hours, minutes, seconds and milliseconds
		int hour = Integer.parseInt(isoDate.substring(offset += 1, offset += 2));
		if (isoDate.charAt(offset) != ':') {
			throw new IndexOutOfBoundsException("Expected : character but found " + isoDate.charAt(offset));
		}
		int minutes = Integer.parseInt(isoDate.substring(offset += 1, offset += 2));
		if (isoDate.charAt(offset) != ':') {
			throw new IndexOutOfBoundsException("Expected : character but found " + isoDate.charAt(offset));
		}
		int seconds = Integer.parseInt(isoDate.substring(offset += 1, offset += 2));
		if (isoDate.charAt(offset) != '.') {
			throw new IndexOutOfBoundsException("Expected . character but found " + isoDate.charAt(offset));
		}
		int milliseconds = Integer.parseInt(isoDate.substring(offset += 1, offset += 3));

		// extract timezone
		String timezoneId;
		char timezoneIndicator = isoDate.charAt(offset);
		if (timezoneIndicator == '+' || timezoneIndicator == '-') {
			timezoneId = "GMT" + isoDate.substring(offset);
		} else if (timezoneIndicator == 'Z') {
			timezoneId = "GMT";
		} else {
			throw new IndexOutOfBoundsException("Invalid time zone indicator " + timezoneIndicator);
		}

		// Get the timezone
		Map<String, TimeZone> timezoneMap = timezones.get();
		if (timezoneMap == null) {
			timezoneMap = new HashMap<>(4);
			timezones.set(timezoneMap);
		}
		TimeZone timezone = timezoneMap.get(timezoneId);
		if (timezone == null) {
			timezone = TimeZone.getTimeZone(timezoneId);
			timezoneMap.put(timezoneId, timezone);
		}
		if (!timezone.getID().equals(timezoneId)) {
			throw new IndexOutOfBoundsException();
		}
		if (!timezone.getID().equals(timezoneId)) {
			throw new IndexOutOfBoundsException();
		}

		// initialize Calendar object#
		// Note: always de-serialise from Gregorian Calendar
		Calendar calendar = new GregorianCalendar(timezone);
		calendar.setLenient(false);
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month - 1);
		calendar.set(Calendar.DAY_OF_MONTH, day);
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minutes);
		calendar.set(Calendar.SECOND, seconds);
		calendar.set(Calendar.MILLISECOND, milliseconds);

		// extract the date
		parsed = calendar.getTime();

		return parsed;
	}

}

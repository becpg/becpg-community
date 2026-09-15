package fr.becpg.repo.helper.json;

import java.util.Collections;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * <p>JsonData class.</p>
 *
 * @author matthieu
 */
public class JsonData implements Iterable<JsonData> {

	private final JsonNode jsonNode;

	/**
	 * <p>Constructor for JsonData.</p>
	 *
	 * @param jsonNode a {@link com.fasterxml.jackson.databind.JsonNode} object
	 */
	public JsonData(JsonNode jsonNode) {
		this.jsonNode = jsonNode;
	}

	/**
	 * <p>get.</p>
	 *
	 * @param path a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.helper.json.JsonData} object
	 */
	public JsonData get(String path) {
		return new JsonData(jsonNode.get(path));
	}

	/**
	 * <p>getString.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getString() {
		return jsonNode.asText();
	}

	/**
	 * <p>getString.</p>
	 *
	 * @param defaultValue a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	public String getString(String defaultValue) {
		return jsonNode.asText(defaultValue);
	}
	
	/**
	 * <p>getBoolean.</p>
	 *
	 * @return a boolean
	 */
	public boolean getBoolean() {
		return jsonNode.asBoolean();
	}
	
	/**
	 * <p>getLong.</p>
	 *
	 * @return a long
	 */
	public long getLong() {
		return jsonNode.asLong();
	}
	
	/**
	 * <p>getInt.</p>
	 *
	 * @return a int
	 */
	public int getInt() {
		return jsonNode.asInt();
	}

	/**
	 * <p>has.</p>
	 *
	 * @param path a {@link java.lang.String} object
	 * @return a boolean
	 */
	public boolean has(String path) {
		return jsonNode.has(path);
	}

	/**
	 * <p>put.</p>
	 *
	 * @param field a {@link java.lang.String} object
	 * @param value a {@link fr.becpg.repo.helper.json.JsonData} object
	 */
	public void put(String field, JsonData value) {
		if (jsonNode instanceof ObjectNode objectNode) {
			objectNode.set(field, value.jsonNode);
		} else {
			throw new UnsupportedOperationException("put() only works on ObjectNode");
		}
	}

	/**
	 * <p>put.</p>
	 *
	 * @param field a {@link java.lang.String} object
	 * @param value a {@link java.lang.String} object
	 */
	public void put(String field, String value) {
		if (jsonNode instanceof ObjectNode objectNode) {
			objectNode.put(field, value);
		} else {
			throw new UnsupportedOperationException("put() only works on ObjectNode");
		}
	}
	
	/**
	 * <p>put.</p>
	 *
	 * @param field a {@link java.lang.String} object
	 * @param value a int
	 */
	public void put(String field, int value) {
		if (jsonNode instanceof ObjectNode objectNode) {
			objectNode.put(field, value);
		} else {
			throw new UnsupportedOperationException("put() only works on ObjectNode");
		}
	}

	/**
	 * <p>fieldNames.</p>
	 *
	 * @return a {@link java.util.Iterator} object
	 */
	public Iterator<String> fieldNames() {
		if (jsonNode instanceof ObjectNode objectNode) {
			return objectNode.fieldNames();
		}
		return Collections.emptyIterator();
	}

	/**
	 * <p>Getter for the field <code>jsonNode</code>.</p>
	 *
	 * @return a {@link com.fasterxml.jackson.databind.JsonNode} object
	 */
	public JsonNode getJsonNode() {
		return jsonNode;
	}

	/**
	 * <p>remove.</p>
	 *
	 * @param field a {@link java.lang.String} object
	 */
	public void remove(String field) {
		if (jsonNode instanceof ObjectNode objectNode) {
			objectNode.remove(field);
		} else {
			throw new UnsupportedOperationException("remove() only works on ObjectNode");
		}
	}

	/**
	 * <p>isEmpty.</p>
	 *
	 * @return a boolean
	 */
	public boolean isEmpty() {
		return jsonNode == null || jsonNode.isEmpty();
	}

	/**
	 * <p>put.</p>
	 *
	 * @param value a {@link fr.becpg.repo.helper.json.JsonData} object
	 */
	public void put(JsonData value) {
		if (jsonNode instanceof ArrayNode arrayNode) {
			arrayNode.add(value.jsonNode);
		} else {
			throw new UnsupportedOperationException("add() only works on ArrayNode");
		}
	}

	/** {@inheritDoc} */
	@Override
	public Iterator<JsonData> iterator() {
		if (jsonNode != null && jsonNode.isArray()) {
			Iterator<JsonNode> it = jsonNode.elements();
			return new Iterator<JsonData>() {
				public boolean hasNext() {
					return it.hasNext();
				}

				public JsonData next() {
					return new JsonData(it.next());
				}
			};
		}
		return java.util.Collections.emptyIterator();
	}

	/** {@inheritDoc} */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		JsonData other = (JsonData) obj;
		return jsonNode.equals(other.jsonNode);
	}

	/**
	 * <p>hashCode.</p>
	 *
	 * @return a int
	 */
	public int hashCode() {
		return jsonNode.hashCode();
	}

	/**
	 * <p>toString.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String toString() {
		return jsonNode.toString();
	}
}

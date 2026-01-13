package fr.becpg.repo.helper.json;

import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonData implements Iterable<JsonData> {

	private final JsonNode jsonNode;

	public JsonData(JsonNode jsonNode) {
		this.jsonNode = jsonNode;
	}

	public JsonData get(String path) {
		return new JsonData(jsonNode.get(path));
	}

	public String getString() {
		return jsonNode.asText();
	}

	public String getString(String defaultValue) {
		return jsonNode.asText(defaultValue);
	}
	
	public boolean getBoolean() {
		return jsonNode.asBoolean();
	}
	
	public long getLong() {
		return jsonNode.asLong();
	}
	
	public int getInt() {
		return jsonNode.asInt();
	}

	public boolean has(String path) {
		return jsonNode.has(path);
	}

	public void put(String field, JsonData value) {
		if (jsonNode instanceof ObjectNode objectNode) {
			objectNode.set(field, value.jsonNode);
		} else {
			throw new UnsupportedOperationException("put() only works on ObjectNode");
		}
	}

	public void put(String field, String value) {
		if (jsonNode instanceof ObjectNode objectNode) {
			objectNode.put(field, value);
		} else {
			throw new UnsupportedOperationException("put() only works on ObjectNode");
		}
	}
	
	public void put(String field, int value) {
		if (jsonNode instanceof ObjectNode objectNode) {
			objectNode.put(field, value);
		} else {
			throw new UnsupportedOperationException("put() only works on ObjectNode");
		}
	}

	public void put(JsonData value) {
		if (jsonNode instanceof ArrayNode arrayNode) {
			arrayNode.add(value.jsonNode);
		} else {
			throw new UnsupportedOperationException("add() only works on ArrayNode");
		}
	}

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

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		JsonData other = (JsonData) obj;
		return jsonNode.equals(other.jsonNode);
	}

	public int hashCode() {
		return jsonNode.hashCode();
	}

	public String toString() {
		return jsonNode.toString();
	}
}

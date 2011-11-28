package fr.becpg.repo.listvalue;

import java.util.List;
import java.util.Map;

public interface ListValueExtractor<T> {

	Map<String, String> extract(List<T> values);

}

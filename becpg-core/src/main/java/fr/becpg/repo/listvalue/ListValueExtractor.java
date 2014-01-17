package fr.becpg.repo.listvalue;

import java.util.List;

public interface ListValueExtractor<T> {

	List<ListValueEntry> extract(List<T> values);

}

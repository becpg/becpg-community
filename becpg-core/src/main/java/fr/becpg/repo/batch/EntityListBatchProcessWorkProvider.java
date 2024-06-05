package fr.becpg.repo.batch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.springframework.lang.NonNull;

/**
 * <p>EntityListBatchProcessWorkProvider class.</p>
 *
 * @author matthieu
 * Split lists into several batch of batch size (processing order is not guaranted)
 * @param <T>
 * @version $Id: $Id
 */
public class EntityListBatchProcessWorkProvider<T> implements BatchProcessWorkProvider<T> {


	private final int size;

	private final Iterator<T> iterator;

	/**
	 * <p>Constructor for EntityListBatchProcessWorkProvider.</p>
	 *
	 * @param entities a {@link java.util.List} object
	 */
	public EntityListBatchProcessWorkProvider(@NonNull List<T> entities) {
		super();
		this.size = entities.size();
		this.iterator = entities.iterator();
	}


	/** {@inheritDoc} */
	@Override
	public Collection<T> getNextWork() {
		Collection<T> results = new ArrayList<>(BatchInfo.BATCH_SIZE);
		while ((results.size() < BatchInfo.BATCH_SIZE) && iterator.hasNext()) {
			results.add(iterator.next());
		}
		return results;
	}

	/** {@inheritDoc} */
	@Override
	public int getTotalEstimatedWorkSize() {
		return size;
	}
	
		/** {@inheritDoc} */
	@Override
		public long getTotalEstimatedWorkSizeLong() {
			return getTotalEstimatedWorkSize();
		}

}

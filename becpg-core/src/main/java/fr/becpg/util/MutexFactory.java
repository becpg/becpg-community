package fr.becpg.util;

import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Component;
import org.springframework.util.ConcurrentReferenceHashMap;

/**
 * <p>MutexFactory class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Component
public class MutexFactory {

    private ConcurrentReferenceHashMap<String, ReentrantLock> map;

    /**
     * <p>Constructor for MutexFactory.</p>
     */
    public MutexFactory() {
        this.map = new ConcurrentReferenceHashMap<>();
    }

    /**
     * <p>getMutex.</p>
     *
     * @param key a {@link java.lang.String} object
     * @return a {@link java.util.concurrent.locks.ReentrantLock} object
     */
    public ReentrantLock getMutex(String key) {
        return this.map.compute(key, (k, v) -> v == null ? new ReentrantLock() : v);
    }
    
    /**
     * <p>removeMutex.</p>
     *
     * @param key a {@link java.lang.String} object
     * @param value a {@link java.lang.Object} object
     */
    public void removeMutex(String key, Object value) {
    	this.map.remove(key, value);
    }
}

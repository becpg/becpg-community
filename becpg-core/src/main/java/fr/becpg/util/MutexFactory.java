package fr.becpg.util;

import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Component;
import org.springframework.util.ConcurrentReferenceHashMap;

@Component
public class MutexFactory {

    private ConcurrentReferenceHashMap<String, ReentrantLock> map;

    public MutexFactory() {
        this.map = new ConcurrentReferenceHashMap<>();
    }

    public ReentrantLock getMutex(String key) {
        return this.map.compute(key, (k, v) -> v == null ? new ReentrantLock() : v);
    }
}
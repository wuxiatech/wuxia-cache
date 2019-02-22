package cn.wuxia.common.cached.memcached;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thimbleware.jmemcached.CacheImpl;
import com.thimbleware.jmemcached.Key;
import com.thimbleware.jmemcached.LocalCacheElement;
import com.thimbleware.jmemcached.MemCacheDaemon;
import com.thimbleware.jmemcached.storage.CacheStorage;
import com.thimbleware.jmemcached.storage.hash.ConcurrentLinkedHashMap;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * 
 * @author songlin
 *
 */
public class MemcachedTestServer {
	private static Logger logger = LoggerFactory.getLogger(MemcachedTestServer.class);

	public static final long DEFAULT_STARTUP_TIMEOUT = 10000;
	private static final int DEFAULT_STORAGE_CAPACITY = 1000;
	private static final long DEFAULT_STORAGE_MEMORY_CAPACITY = 10000;

	private MemCacheDaemon<LocalCacheElement> memcacheDaemon;

	public void start(final String host, final int port) {
		logger.debug("本机启动memcached服务端...");

		final CountDownLatch startupLatch = new CountDownLatch(1);
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.execute(new Runnable() {
			@Override
			public void run() {
				CacheStorage<Key, LocalCacheElement> storage = ConcurrentLinkedHashMap.create(
						ConcurrentLinkedHashMap.EvictionPolicy.FIFO, DEFAULT_STORAGE_CAPACITY,
						DEFAULT_STORAGE_MEMORY_CAPACITY);
				memcacheDaemon = new MemCacheDaemon<>();
				memcacheDaemon.setCache(new CacheImpl(storage));
				memcacheDaemon.setAddr(new InetSocketAddress(host, port));
				memcacheDaemon.start();
				startupLatch.countDown();
			}
		});
		try {
			if (!startupLatch.await(DEFAULT_STARTUP_TIMEOUT, MILLISECONDS)) {
				logger.error("Memcache daemon did not start after {}ms. Consider increasing the timeout", MILLISECONDS);
				throw new AssertionError("Memcache daemon did not start within timeout");
			}
		} catch (InterruptedException e) {
			logger.error("Interrupted waiting for Memcache daemon to start:", e);
			throw new AssertionError(e);
		}
	}

	public void clean() {
		if (memcacheDaemon != null) {
			logger.debug("Cleaning memcache...");
			memcacheDaemon.getCache().flush_all();
		}
	}
}

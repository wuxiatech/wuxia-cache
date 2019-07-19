package cn.wuxia.common.cached;

public interface CacheClient {
    boolean containKey(String key, String namespace);
    boolean containKey(String key);

    void add(String key, Object value, int expiredTime, String namespace);
    void add(String key, Object value, int expiredTime);
    void add(String key, Object value, String namespace);
    void add(String key, Object value);


    void set(String key, Object value, int expiredTime, String namespace);
    void set(String key, Object value, int expiredTime);
    void set(String key, Object value, String namespace);
    void set(String key, Object value);

    void replace(String key, Object value, int expiredTime, String namespace);
    void replace(String key, Object value, int expiredTime);
    void replace(String key, Object value,  String namespace);
    void replace(String key, Object value);

    public <T> T get(String key, String namespace);
    public <T> T get(String key);

    void delete(String key, String namespace);
    void delete(String key);


    public long incr(String key);
    public long incr(String key, String namespace);
    public long incr(String key, long by);
    public long incr(String key, long by, long defaultValue);
    public long incr(String key, long by, long defaultValue, String namespace);

    public long decr(String key);
    public long decr(String key, String namespace);
    public long decr(String key, long by);
    public long decr(String key, long by, long defaultValue);
    public long decr(String key, long by, long defaultValue, String namespace);

    void flush(String namespace);

    void flushAll();

    void flushAll(String[] servers);

    void init(String... servers);

    void shutdown();
}

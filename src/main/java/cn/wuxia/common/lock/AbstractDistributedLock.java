package cn.wuxia.common.lock;

import java.util.concurrent.TimeUnit;

public abstract class AbstractDistributedLock {
    public abstract void unlock(String lockKey) ;
    public abstract boolean lock(String lockKey, TimeUnit unit, long waitTime, long leaseTime) ;
}

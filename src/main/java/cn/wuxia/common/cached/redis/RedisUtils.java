/*
 * Created on :1 Sep, 2015
 * Author     :songlin
 * Change History
 * Version       Date         Author           Reason
 * <Ver.No>     <date>        <who modify>       <reason>
 * Copyright 2014-2020 武侠科技 All right reserved.
 */
package cn.wuxia.common.cached.redis;

import cn.wuxia.common.cached.memcached.MemcachedException;
import cn.wuxia.common.util.StringUtil;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;

public class RedisUtils {
    public final static int MAX_KEY_LENGTH = 128;

    /**
     * 校验key是否符合规则
     *
     * @param key
     * @author songlin
     */
    public static void validateKey(String key) {
        if (StringUtil.isBlank(key)) {
            throw new MemcachedException("Key must contain at least one character.");
        }
        byte[] keyBytes = StringUtils.getBytesUtf8(key);

        if (keyBytes.length > MAX_KEY_LENGTH) {
            throw new IllegalArgumentException("Key is too long (maxlen = " + MAX_KEY_LENGTH + ")");
        }
        // Validate the key
        for (byte b : keyBytes) {
            if (b == ' ' || b == '\n' || b == '\r' || b == 0) {
                throw new IllegalArgumentException("Key contains invalid characters:  ``" + key + "''");
            }
        }
    }

    /**
     * 校验key是否存在不符合规则的字符
     *
     * @param key
     * @author songlin
     */
    public static boolean hasControlChar(String key) {
        if (StringUtil.isBlank(key)) {
            throw new MemcachedException("Key must contain at least one character.");
        }
        byte[] keyBytes = StringUtils.getBytesUtf8(key);
        // Validate the key
        for (byte b : keyBytes) {
            if (b == ' ' || b == '\n' || b == '\r' || b == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 格式化key格式，将key中不允许的字符或分割变为:处理
     *
     * @author songlin
     */
    public static String formatKey(String key) {
        validateKey(key);
        return StringUtil.replace(key, ".", ":");
    }
}

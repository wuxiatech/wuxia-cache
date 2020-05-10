/*
 * Created on :1 Sep, 2015
 * Author     :songlin
 * Change History
 * Version       Date         Author           Reason
 * <Ver.No>     <date>        <who modify>       <reason>
 * Copyright 2014-2020 武侠科技 All right reserved.
 */
package cn.wuxia.common.cached.memcached;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;

import cn.wuxia.common.util.StringUtil;

public class MemcachedUtils {
    public final static int MAX_KEY_LENGTH = 250;

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
     * sha1加密,返回40位十六进制字符串,防止key值过长或者不合法的傻瓜式处理
     *
     * @author songlin
     */
    public static String shaKey(String key) {
        return DigestUtils.sha1Hex(org.apache.commons.codec.binary.StringUtils.getBytesUtf8(key));
    }

    /**
     * 将127.0.0.1:11211,127.0.0.1:11211 -> 127.0.0.1:11211 127.0.0.1:11211
     *
     * @param servers
     * @return
     */
    public static String formatServerUrl(String servers) {
        return StringUtil.replace(servers, ",", " ");
    }

    /**
     * 将127.0.0.1:11211,127.0.0.1:11211 -> [127.0.0.1:11211,127.0.0.1:11211]
     *
     * @param servers
     * @return
     */
    public static String[] serverUrl2Array(String servers) {
        if (StringUtil.indexOf(servers, ",") > 0) {
            return StringUtil.split(servers, ",");
        } else if (StringUtil.indexOf(servers, " ") > 0) {
            return StringUtil.split(servers, " ");
        } else {
            return new String[]{servers};
        }
    }
}

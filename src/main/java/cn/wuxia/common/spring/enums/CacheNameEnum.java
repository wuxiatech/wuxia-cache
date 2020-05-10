/*
 * Created on :Jun 25,   ), 2013 Author :PL Change History Version Date Author Reason
 * <Ver.No> <date> <who modify> <reason>
 */
package cn.wuxia.common.spring.enums;

import lombok.Getter;

/**
 * @author songlin
 */
@Getter
public enum CacheNameEnum {
    /**
     * 30秒
     */
    CACHE_30_SECONDS("30SecondsData", 30),
    /**
     * 1分钟
     */
    CACHE_1_MINUTES("1MinutesData", 1 * 60),
    /**
     * 2分钟
     */
    CACHE_2_MINUTES("2MinutesData", 2 * 60),
    /**
     * 10分钟
     */
    CACHE_10_MINUTES("10MinutesData", 10 * 60),
    /**
     * 30分钟
     */
    CACHE_30_MINUTES("30MinutesData", 30 * 60),
    /**
     * 60分钟
     */
    CACHE_1_HOUR("1HourData", 1 * 60 * 60),
    /**
     * 2小时
     */
    CACHE_2_HOUR("2HourData", 2 * 60 * 60),
    /**
     * 4小时
     */
    CACHE_4_HOUR("4HourData", 4 * 60 * 60),
    /**
     * 1天
     */
    CACHE_1_DAY("1DayData", 24 * 60 * 60);
    private String cacheName;
    private int expiredTime;
    private int ttl;

    CacheNameEnum(String cacheName, int expiredTime) {
        this.cacheName = cacheName;
        this.expiredTime = expiredTime;
        this.ttl = this.expiredTime * 1000;
    }
}

package com.analysys.allgro.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 点击采集忽略注解（可标注页面类&回调方法）
 * @author fengzeyuan
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)

public @interface AnalysysIgnoreTrackClick {
}

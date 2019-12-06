package com.analysys.allgro.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 页面自动采集忽略注解
 * @author fengzeyuan
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)

public @interface AnalysysIgnorePage {
}

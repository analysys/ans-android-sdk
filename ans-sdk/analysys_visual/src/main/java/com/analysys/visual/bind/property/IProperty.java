package com.analysys.visual.bind.property;

/**
 * @Copyright © 2019 Analysys Inc. All rights reserved.
 * @Description: 属性接口
 * @Create: 2019-11-28 10:34
 * @author: hcq
 */
public interface IProperty {

    String getName();

    Object getValue(Object obj);

    Object getMatchValue();

    boolean isMatch(Object obj);
}

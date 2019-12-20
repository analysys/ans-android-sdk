package com.analysys;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2018/9/13 18:37
 * @Author: Wang-X-C
 */
public enum EncryptEnum {
    /**
     * agree
     */
    EMPTY(0), AES(1), AES_CBC(2);

    private final int type;

    EncryptEnum(int name) {
        this.type = name;
    }

    public int getType() {
        return type;
    }

}

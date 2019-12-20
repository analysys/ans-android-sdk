package com.analysys.database;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: 数据库 常量
 * @Version: 1.0
 * @Create: 2018/3/20 19:22
 * @Author: WXC
 */
class DBConfig {

    static final class TableAllInfo {
        static final String DBNAME = "analysys.data";
        static final int VERSION = 1;

        static final String TABLE_NAME = " e_fz ";
        //建表
        static final String CREATE_TABLE = "create table if not exists " +
                TABLE_NAME + " (" +
                Column.ID + Types.ID + "," +
                Column.INFO + Types.INFO + "," +
                Column.SIGN + Types.SIGN + "," +
                Column.TYPE + Types.TYPE + "," +
                Column.INSERT_DATE + Types.INSERT_DATE + "," +
                Column.RESERVE_A + Types.RESERVE_A + "," +
                Column.RESERVE_B + Types.RESERVE_B + "," +
                Column.RESERVE_C + Types.RESERVE_C + "  )";

        // 列名
        static class Column {
            static final String ID = "id";
            static final String INFO = "a";
            static final String SIGN = "b";
            static final String TYPE = "c";
            static final String INSERT_DATE = "d";
            static final String RESERVE_A = "r_a";
            static final String RESERVE_B = "r_b";
            static final String RESERVE_C = "r_c";
        }

        // 类型
        static class Types {
            static final String ID = " Integer Primary Key Autoincrement ";
            static final String INFO = " text ";
            static final String SIGN = " int not null ";
            static final String TYPE = " varchar(50) not null ";
            static final String INSERT_DATE = " varchar(50) not null ";
            static final String RESERVE_A = " text ";
            static final String RESERVE_B = " text ";
            static final String RESERVE_C = " text ";
        }
    }

    /**
     * 存储数据状态。
     */
    final static class Status {
        static final int FLAG_SAVE = 0;
        static final int FLAG_UPLOADING = -1;
    }
}

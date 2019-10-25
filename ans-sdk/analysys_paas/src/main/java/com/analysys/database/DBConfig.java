package com.analysys.database;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: 数据库 常量
 * @Version: 1.0
 * @Create: 2018/3/20 19:22
 * @Author: WXC
 */
class DBConfig {

    public static class TableAllInfo {
        public static final String TABLE_NAME = " e_fz ";
        //建表
        public static final String CREATE_TABLE = "create table if not exists " +
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
        public static class Column {
            public static final String ID = "id";
            public static final String INFO = "a";
            public static final String SIGN = "b";
            public static final String TYPE = "c";
            public static final String INSERT_DATE = "d";
            public static final String RESERVE_A = "r_a";
            public static final String RESERVE_B = "r_b";
            public static final String RESERVE_C = "r_c";
        }

        // 类型
        public static class Types {
            public static final String ID = " Integer Primary Key Autoincrement ";
            public static final String INFO = " text ";
            public static final String SIGN = " int not null ";
            public static final String TYPE = " varchar(50) not null ";
            public static final String INSERT_DATE = " varchar(50) not null ";
            public static final String RESERVE_A = " text ";
            public static final String RESERVE_B = " text ";
            public static final String RESERVE_C = " text ";
        }
    }

    /**
     * 存储数据状态。
     */
    public static class Status {
        public static final int FLAG_SAVE = 0;
        public static final int FLAG_UPLOADING = -1;
    }
}

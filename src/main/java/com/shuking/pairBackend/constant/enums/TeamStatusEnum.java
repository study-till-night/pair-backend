package com.shuking.pairBackend.constant.enums;

/**
 * 队伍状态枚举类
 */
public enum TeamStatusEnum {

    PUBLIC(0, "公开"),
    PRIVATE(1, "私有"),
    SECRET(2, "加密");

    private final int value;

    private final String description;

    //  必须要有构造函数
    TeamStatusEnum(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public static TeamStatusEnum getEnumByValue(Integer value) {
        if (value == null)
            return null;
        //  获取枚举类所有元素
        TeamStatusEnum[] values = TeamStatusEnum.values();
        for (TeamStatusEnum teamStatusEnum : values) {
            if (teamStatusEnum.getValue() == value)
                return teamStatusEnum;
        }
        return null;
    }

    public int getValue() {
        return this.value;
    }

    public String getDescription() {
        return description;
    }
}

package com.hmall.user.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum UserStatus {
    FROZEN(0, "禁止使用"),
    NORMAL(1, "已激活");

    @EnumValue
    int value;
    String msg;

    UserStatus(int value, String msg) {
        this.value = value;
        this.msg = msg;
    }
}
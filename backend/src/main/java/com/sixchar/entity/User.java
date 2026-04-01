package com.sixchar.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String account;
    private String password;
    private String role;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

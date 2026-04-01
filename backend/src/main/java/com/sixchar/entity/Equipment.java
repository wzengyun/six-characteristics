package com.sixchar.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("equipment")
public class Equipment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String model;
    private String structure;
    private String parts;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

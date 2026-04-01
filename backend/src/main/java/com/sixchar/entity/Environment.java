package com.sixchar.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("environment")
public class Environment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long equipmentId;
    private String temp;
    private String vibration;
    private String humidity;
    private Double adaptScore;
    private String result;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

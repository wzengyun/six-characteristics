package com.sixchar.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("supportability")
public class Supportability {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long equipmentId;
    private String resource;
    private Double cost;
    private Double supportRate;
    private String result;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

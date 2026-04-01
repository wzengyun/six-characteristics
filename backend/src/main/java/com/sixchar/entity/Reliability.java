package com.sixchar.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("reliability")
public class Reliability {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long equipmentId;
    private Double mtbf;
    private Double rate;
    private String reliabilityCurve;
    private String params;
    private String result;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

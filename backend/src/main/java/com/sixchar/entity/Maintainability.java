package com.sixchar.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("maintainability")
public class Maintainability {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long equipmentId;
    private Double mttr;
    private Double repairRate;
    private String process;
    private String result;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

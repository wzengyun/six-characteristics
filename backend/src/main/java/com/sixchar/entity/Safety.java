package com.sixchar.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("safety")
public class Safety {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long equipmentId;
    private String ftaTree;
    private String minCutSet;
    private Double failureProb;
    private String result;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

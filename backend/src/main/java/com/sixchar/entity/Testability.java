package com.sixchar.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("testability")
public class Testability {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long equipmentId;
    private Double fdr;
    private Double fir;
    private String testPoint;
    private String result;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

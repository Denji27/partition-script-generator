package org.example;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class RangePartitionRequirement {
    private String tableName;
    private PartitionType partitionType;
    private DataType dataType;
    private String selectedColumn;
    private Integer partitionQuantity;

    private LocalDate from;
    private LocalDate to;
}

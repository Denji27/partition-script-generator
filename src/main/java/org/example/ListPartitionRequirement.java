package org.example;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ListPartitionRequirement {
    private String tableName;
    private PartitionType partitionType;
    private List<String> values;
}

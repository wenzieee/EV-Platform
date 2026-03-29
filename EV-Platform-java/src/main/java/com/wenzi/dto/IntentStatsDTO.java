package com.wenzi.dto;

import lombok.Data;
import java.util.List;

@Data
public class IntentStatsDTO {
    private List<BrandStats> brandStats;
    private List<ModelStats> modelStats;
    private List<StatusStats> statusStats;

    @Data
    public static class BrandStats {
        private String brand;
        private Long count;
    }

    @Data
    public static class ModelStats {
        private String model;
        private Long count;
    }

    @Data
    public static class StatusStats {
        private Byte status;
        private String statusText; // 用于前端显示状态名称
        private Long count;
    }
}

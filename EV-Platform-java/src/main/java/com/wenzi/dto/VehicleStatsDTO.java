package com.wenzi.dto;

import lombok.Data;

import java.util.List;

@Data
public class VehicleStatsDTO {
    // 品牌统计，用于柱状图
    private List<BrandStats> brandStats;

    // 驱动类型统计，用于饼图
    private List<DriveTypeStats> driveTypeStats;

    @Data
    public static class BrandStats {
        private String brand;
        private Long count;
    }

    @Data
    public static class DriveTypeStats {
        private String driveType;
        private Long count;
    }
}

package com.wenzi.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserStatsDTO {
    private List<RoleStats> roleStats;
    private List<StatusStats> statusStats;

    @Data
    public static class RoleStats {
        private Byte role;
        private String roleText;
        private Long count;
    }

    @Data
    public static class StatusStats {
        private Byte status;
        private String statusText;
        private Long count;
    }
}

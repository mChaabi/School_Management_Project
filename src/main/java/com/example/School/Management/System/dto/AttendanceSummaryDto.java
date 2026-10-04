package com.example.School.Management.System.dto;

public record AttendanceSummaryDto(long present, long absent, long late, long excused) {
    public long total() { return present + absent + late + excused; }
    public double percentage() {
        // Present + Late + Excused count as "attended"; change the rule if your school differs
        return total() == 0 ? 0 : (present + late + excused) * 100.0 / total();
    }
}
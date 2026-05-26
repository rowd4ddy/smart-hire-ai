package com.smarthireai.dto;

import java.time.LocalDate;
import java.util.List;

public record CreateJobRequest(
        String title,
        String company,
        List<String> requiredSkills,
        Integer minimumExperienceYears,
        String educationLevel,
        String location,
        String department,
        String employmentType,
        String workMode,
        String salaryRange,
        LocalDate applicationDeadline,
        String status
) {
}

package com.smarthireai.dto;

import java.util.List;

public record RankedCandidateDto(
        Long candidateId,
        String fullName,
        String email,
        List<String> skills,
        int experienceYears,
        String educationLevel,
        double finalScore,
        double skillsScore,
        double experienceScore,
        double educationScore,
        List<String> missingSkills
) {}

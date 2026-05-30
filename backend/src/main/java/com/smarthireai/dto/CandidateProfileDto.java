package com.smarthireai.dto;

import java.util.List;

public record CandidateProfileDto(
        Long id,
        String fullName,
        String email,
        List<String> skills,
        int experienceYears,
        String educationLevel
) {}

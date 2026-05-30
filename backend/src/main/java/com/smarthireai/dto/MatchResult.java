package com.smarthireai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record MatchResult(
        @JsonProperty("final_score") double finalScore,
        @JsonProperty("skills_score") double skillsScore,
        @JsonProperty("experience_score") double experienceScore,
        @JsonProperty("education_score") double educationScore,
        @JsonProperty("missing_skills") List<String> missingSkills
) {}

package com.smarthireai.controller;

import com.smarthireai.dto.RankedCandidateDto;
import com.smarthireai.service.MatchService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping("/{jobId}/ranked-candidates")
    public List<RankedCandidateDto> getRankedCandidates(@PathVariable Long jobId) {
        return matchService.getRankedCandidatesForJob(jobId);
    }
}

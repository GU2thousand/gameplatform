package com.gamingplatform.dto;

import java.util.List;
import java.util.Map;

public record AttemptComparisonResponse(
        SubmissionStatusResponse first,
        SubmissionStatusResponse second,
        double scoreDelta,
        Map<String, Double> rubricDeltas,
        List<String> improvedDimensions,
        List<String> declinedDimensions
) {}

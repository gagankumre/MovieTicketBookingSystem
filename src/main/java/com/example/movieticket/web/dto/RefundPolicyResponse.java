package com.example.movieticket.web.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RefundPolicyResponse {

    Long id;
    int hoursBeforeShow;
    int refundPercent;
}

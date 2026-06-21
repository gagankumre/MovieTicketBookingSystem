package com.example.movieticket.mapper;

import com.example.movieticket.domain.RefundPolicy;
import com.example.movieticket.web.dto.RefundPolicyResponse;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RefundPolicyMapper {

    RefundPolicyResponse toResponse(RefundPolicy refundPolicy);

    List<RefundPolicyResponse> toResponseList(List<RefundPolicy> refundPolicies);
}

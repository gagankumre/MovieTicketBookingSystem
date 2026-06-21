package com.example.movieticket.mapper;

import com.example.movieticket.domain.DiscountCode;
import com.example.movieticket.web.dto.DiscountCodeResponse;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DiscountCodeMapper {

    DiscountCodeResponse toResponse(DiscountCode discountCode);

    List<DiscountCodeResponse> toResponseList(List<DiscountCode> discountCodes);
}

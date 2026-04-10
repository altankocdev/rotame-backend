package com.altankoc.rotame.location.mapper;

import com.altankoc.rotame.location.dto.LocationImageResponse;
import com.altankoc.rotame.location.entity.LocationImage;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LocationImageMapper {
    LocationImageResponse toResponse(LocationImage locationImage);
}
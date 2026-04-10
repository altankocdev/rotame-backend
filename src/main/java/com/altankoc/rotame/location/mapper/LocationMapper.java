package com.altankoc.rotame.location.mapper;

import com.altankoc.rotame.location.dto.CreateLocationRequest;
import com.altankoc.rotame.location.dto.LocationResponse;
import com.altankoc.rotame.location.dto.UpdateLocationRequest;
import com.altankoc.rotame.location.entity.Location;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {LocationImageMapper.class}
)
public interface LocationMapper {

    @Mapping(target = "images", source = "locationImages")
    LocationResponse toResponse(Location location);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "favorite", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "locationImages", ignore = true)
    Location toEntity(CreateLocationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "favorite", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "locationImages", ignore = true)
    void updateEntity(UpdateLocationRequest request, @MappingTarget Location location);
}
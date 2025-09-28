package com.comix.scrapers.bedetheque.rest.mapper;

import com.comix.scrapers.bedetheque.client.model.statistics.GlobalStatistics;
import com.comix.scrapers.bedetheque.rest.v1.dto.DashboardDto;
import org.mapstruct.Mapper;

@Mapper
public interface DashboardMapper {

    DashboardDto globalStatisticsToDashboardDto(GlobalStatistics globalStatistics);
}

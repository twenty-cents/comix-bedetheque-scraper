package com.comix.scrapers.bedetheque.service.impl;

import com.comix.scrapers.bedetheque.client.scraper.GlobalStatisticsScraper;
import com.comix.scrapers.bedetheque.rest.mapper.DashboardMapper;
import com.comix.scrapers.bedetheque.rest.v1.dto.DashboardDto;
import com.comix.scrapers.bedetheque.service.DashboardService;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final GlobalStatisticsScraper globalStatisticsScraper;

    private final DashboardMapper dashboardMapper = Mappers.getMapper(DashboardMapper.class);

    public DashboardServiceImpl(GlobalStatisticsScraper globalStatisticsScraper) {
        this.globalStatisticsScraper = globalStatisticsScraper;
    }

    @Override
    public DashboardDto scrapDashboard() {
        return dashboardMapper.globalStatisticsToDashboardDto(globalStatisticsScraper.scrap());
    }
}

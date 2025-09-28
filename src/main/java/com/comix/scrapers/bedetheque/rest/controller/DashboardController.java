package com.comix.scrapers.bedetheque.rest.controller;

import com.comix.scrapers.bedetheque.rest.v1.api.DashboardApi;
import com.comix.scrapers.bedetheque.rest.v1.dto.DashboardDto;
import com.comix.scrapers.bedetheque.service.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DashboardController implements V1Controller, DashboardApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardController.class);

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * GET /dashboard : Scrap the dashboard page from <a href="https://www.bedetheque.com">...</a>
     *
     * @return the scraped dashboard
     */
    @Override
    public ResponseEntity<DashboardDto> scrapDashboard() {
        LOGGER.info("Bedetheque - Scrap the dashboard page from https://www.bedetheque.com");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(dashboardService.scrapDashboard()
                );
    }

}

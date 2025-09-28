package com.comix.scrapers.bedetheque.service;

import com.comix.scrapers.bedetheque.rest.v1.dto.DashboardDto;
import org.springframework.stereotype.Service;

@Service
public interface DashboardService {

    DashboardDto scrapDashboard();
}

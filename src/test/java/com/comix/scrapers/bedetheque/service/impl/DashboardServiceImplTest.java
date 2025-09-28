package com.comix.scrapers.bedetheque.service.impl;

import com.comix.scrapers.bedetheque.client.model.statistics.GlobalStatistics;
import com.comix.scrapers.bedetheque.client.scraper.GlobalStatisticsScraper;
import com.comix.scrapers.bedetheque.rest.mapper.DashboardMapper;
import com.comix.scrapers.bedetheque.rest.v1.dto.DashboardDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DashboardServiceImpl}.
 * These tests verify that the service correctly interacts with its dependencies (scraper and mapper)
 * to produce the expected result.
 */
@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private GlobalStatisticsScraper globalStatisticsScraper;

    @Mock
    private DashboardMapper dashboardMapper;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    @DisplayName("scrapDashboard should return a mapped DTO when scraper finds data")
    void scrapDashboard_shouldReturnDto_whenScraperFindsData() {
        // GIVEN: We prepare mock data that the scraper and mapper will use.
        GlobalStatistics scrapedStats = new GlobalStatistics(
                91000,
                378980,
                123456,
                7890,
                null,
                null
        );
        DashboardDto expectedDto = new DashboardDto();
        expectedDto.setSeries(91000);
        expectedDto.setGraphicNovels(378980);
        expectedDto.setAuthors(123456);
        expectedDto.setReviews(7890);
        expectedDto.setNews(null);
        expectedDto.setLastEntries(null);
        // ... other fields would be set here by the mapper

        // Configure the mocks' behavior
        when(globalStatisticsScraper.scrap()).thenReturn(scrapedStats);

        // WHEN: The service method is called
        DashboardDto result = dashboardService.scrapDashboard();

        // THEN: We verify the interactions and the final result
        // 1. The scraper was called to get the raw data
        verify(globalStatisticsScraper, times(1)).scrap();

        // 2. The final result is the DTO returned by the mapper
        assertThat(result)
                .isNotNull()
                .isEqualTo(expectedDto);
    }

    @Test
    @DisplayName("scrapDashboard should return null when scraper returns null")
    void scrapDashboard_shouldReturnNull_whenScraperReturnsNull() {
        // GIVEN: The scraper is configured to return null (e.g., page not found or scraping error)
        when(globalStatisticsScraper.scrap()).thenReturn(null);

        // WHEN: The service method is called
        DashboardDto result = dashboardService.scrapDashboard();

        // THEN: We verify the interactions and the result
        // 1. The scraper was still called
        verify(globalStatisticsScraper, times(1)).scrap();
        // 2. The mapper should NEVER be called if there is no data to map
        verify(dashboardMapper, never()).globalStatisticsToDashboardDto(any());

        // 3. The service should return null, propagating the absence of data
        assertThat(result).isNull();
    }
}
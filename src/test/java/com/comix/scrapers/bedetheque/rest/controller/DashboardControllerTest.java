package com.comix.scrapers.bedetheque.rest.controller;

import com.comix.scrapers.bedetheque.rest.v1.dto.DashboardDto;
import com.comix.scrapers.bedetheque.service.DashboardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private DashboardController dashboardController;

    @Test
    @DisplayName("scrapDashboard should call service and return OK with the service's response")
    void scrapDashboard_shouldCallServiceAndReturnOk() {
        // GIVEN: A mock DTO that the service will return
        DashboardDto mockDashboardDto = new DashboardDto();
        // Configure the mock service to return our DTO
        when(dashboardService.scrapDashboard()).thenReturn(mockDashboardDto);

        // WHEN: The controller method is called
        ResponseEntity<DashboardDto> response = dashboardController.scrapDashboard();

        // THEN: Verify the results
        // 1. The service method was called exactly once
        verify(dashboardService, times(1)).scrapDashboard();

        // 2. The response has the correct HTTP status
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 3. The body of the response is the object returned by the service
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEqualTo(mockDashboardDto);
    }

    @Test
    @DisplayName("scrapDashboard should return OK with a null body when the service returns null")
    void scrapDashboard_whenServiceReturnsNull_shouldReturnOkWithNullBody() {
        // GIVEN: The service is configured to return null
        when(dashboardService.scrapDashboard()).thenReturn(null);

        // WHEN: The controller method is called
        ResponseEntity<DashboardDto> response = dashboardController.scrapDashboard();

        // THEN: Verify the results
        // 1. The service method was still called
        verify(dashboardService, times(1)).scrapDashboard();

        // 2. The response status is still OK
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 3. The body of the response is null, as expected
        assertThat(response.getBody()).isNull();
    }
}
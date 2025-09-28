package com.comix.scrapers.bedetheque.rest.mapper.mapper;

import com.comix.scrapers.bedetheque.client.model.statistics.GlobalStatistics;
import com.comix.scrapers.bedetheque.rest.mapper.DashboardMapper;
import com.comix.scrapers.bedetheque.rest.v1.dto.DashboardDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class DashboardMapperTest {

    // On récupère une instance de l'implémentation du mapper générée par MapStruct
    private final DashboardMapper dashboardMapper = Mappers.getMapper(DashboardMapper.class);

    @Test
    @DisplayName("Should map GlobalStatistics to DashboardDto correctly")
    void shouldMapGlobalStatisticsToDto() {
        // GIVEN: un objet source avec des données de test
        GlobalStatistics source = new GlobalStatistics(
                91789,
                378980,
                123456,
                7890,
                null,
                null
        );

        // WHEN: on appelle la méthode de mapping
        DashboardDto destination = dashboardMapper.globalStatisticsToDashboardDto(source);

        // THEN: on vérifie que l'objet de destination est correct
        assertThat(destination).isNotNull();
        assertThat(destination.getSeries()).isEqualTo(source.getSeries());
        assertThat(destination.getGraphicNovels()).isEqualTo(source.getGraphicNovels());
        assertThat(destination.getAuthors()).isEqualTo(source.getAuthors());
        assertThat(destination.getReviews()).isEqualTo(source.getReviews());
    }

    @Test
    @DisplayName("Should return null when GlobalStatistics source is null")
    void shouldReturnNullForNullGlobalStatistics() {
        // GIVEN: une source nulle
        GlobalStatistics source = null;

        // WHEN: on appelle la méthode de mapping
        DashboardDto destination = dashboardMapper.globalStatisticsToDashboardDto(source);

        // THEN: on vérifie que le résultat est bien null
        assertThat(destination).isNull();
    }
}
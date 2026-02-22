package com.comix.scrapers.bedetheque.rest.mapper;

import com.comix.scrapers.bedetheque.client.model.media.Media;
import com.comix.scrapers.bedetheque.client.model.media.MediaType;
import com.comix.scrapers.bedetheque.rest.v1.dto.MediaDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.MediaTypeDto;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class MediaMapperTest {

    private final MediaMapper mapper = Mappers.getMapper(MediaMapper.class);

    @Test
    void mediaToMediaDto_shouldMapCorrectly() {
        // Given
        Media media = new Media();
        // You can set properties here to verify they are mapped correctly
        media.setId("123");
        media.setFilename("image.jpg");
        media.setUrl("http://test.com/image.jpg");
        media.setAvailable(true);
        media.setFileSize(1024L);
        media.setType(MediaType.COMIC_BOOK_COVER);

        // When
        MediaDto result = mapper.mediaToMediaDto(media);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("123");
        assertThat(result.getFilename()).isEqualTo("image.jpg");
        assertThat(result.getUrl()).isEqualTo("http://test.com/image.jpg");
        assertThat(result.getAvailable()).isTrue();
        assertThat(result.getFileSize()).isEqualByComparingTo(String.valueOf(1024L));
        assertThat(result.getType()).isEqualTo(MediaTypeDto.COMIC_BOOK_COVER);
    }

    @Test
    void mediaToMediaDto_shouldReturnNull_whenInputIsNull() {
        // When & Then
        assertThat(mapper.mediaToMediaDto(null)).isNull();
    }

    @Test
    void mediaToMediaDto_shouldMapNullPropertiesCorrectly() {
        // Given a media object with some null fields
        Media media = new Media();
        media.setId("456");
        media.setFilename(null);
        media.setUrl(null);
        media.setAvailable(false);
        media.setFileSize(null);
        media.setType(null);

        // When
        MediaDto result = mapper.mediaToMediaDto(media);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("456");
        assertThat(result.getFilename()).isNull();
        assertThat(result.getUrl()).isNull();
        assertThat(result.getAvailable()).isFalse();
        assertThat(result.getFileSize()).isNull();
        assertThat(result.getType()).isNull();

    }
}
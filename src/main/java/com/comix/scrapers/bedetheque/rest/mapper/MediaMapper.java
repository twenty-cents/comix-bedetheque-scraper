package com.comix.scrapers.bedetheque.rest.mapper;

import com.comix.scrapers.bedetheque.client.model.media.Media;
import com.comix.scrapers.bedetheque.rest.v1.dto.MediaDto;
import org.mapstruct.Mapper;

@Mapper
public interface MediaMapper {

    MediaDto mediaToMediaDto(Media media);
}

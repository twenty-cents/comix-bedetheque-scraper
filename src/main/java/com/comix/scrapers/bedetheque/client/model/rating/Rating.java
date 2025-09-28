package com.comix.scrapers.bedetheque.client.model.rating;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Rating {

    private String graphicNovelTitle;
    private String graphicNovelUrl;

    private String graphicNovelPictureUrl;
    private String graphicNovelPictureTitle;

    private String createBy;
    private String createByAllRatingsUrl;
    private String createOn;

    private String ratingPictureUrl;
    private String ratingTitle;

    private String comment;
}

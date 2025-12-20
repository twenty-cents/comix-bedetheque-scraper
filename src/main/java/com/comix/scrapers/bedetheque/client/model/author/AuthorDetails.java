package com.comix.scrapers.bedetheque.client.model.author;

import com.comix.scrapers.bedetheque.client.model.serie.SerieToDiscover;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"authorsToDiscover", "seriesToDiscover", "bibliography"})
@ToString(exclude = {"authorsToDiscover", "seriesToDiscover", "bibliography"})
public class AuthorDetails {

    private String id;
    private String lastname;
    private String firstname;
    private String nickname;
    private String nationality;
    private String birthdate;
    private String deceaseDate;
    private String biography;
    private String siteUrl;
    private Author otherAuthorPseudonym;

    private String originalPhotoUrl;
    private String photoUrl;
    private Boolean isPhotoUrlChecked;
    private String photoPath;
    private String photoFilename;
    private Long photoFileSize;

    private String authorUrl;
    private List<Author> authorsToDiscover;
    private List<SerieToDiscover> seriesToDiscover;
    private List<Collaboration> bibliography;

    private Author nextAuthor;
    private Author previousAuthor;
}


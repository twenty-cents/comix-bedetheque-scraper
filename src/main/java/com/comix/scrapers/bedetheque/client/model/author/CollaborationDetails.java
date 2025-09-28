package com.comix.scrapers.bedetheque.client.model.author;

import com.comix.scrapers.bedetheque.client.model.serie.SerieLanguage;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"roles"})
@ToString(exclude = {"roles"})
public class CollaborationDetails {

    private String id;
    private String title;
    private String serieUrl;
    private SerieLanguage language;
    private String fromYear;
    private String toYear;
    private List<String> roles;
}

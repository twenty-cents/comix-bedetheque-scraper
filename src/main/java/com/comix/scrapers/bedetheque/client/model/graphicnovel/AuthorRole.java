package com.comix.scrapers.bedetheque.client.model.graphicnovel;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AuthorRole {

    private String externalId;
    private String role;
    private String displayedRole;
    private String name;
    private String authorUrl;

}

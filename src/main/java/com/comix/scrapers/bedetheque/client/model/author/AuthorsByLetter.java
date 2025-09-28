package com.comix.scrapers.bedetheque.client.model.author;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AuthorsByLetter {

    private String letter;
    private String url;
}

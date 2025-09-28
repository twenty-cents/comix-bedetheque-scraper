package com.comix.scrapers.bedetheque.client.model.author;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Author implements Comparable<Author> {

    private String id;
    private String name;
    private String url;

    public int compareTo(Author a2){
        if(name.compareTo(a2.name) == 0) {
            return 0;
        } else if(name.compareTo(a2.name) > 0) {
            return 1;
        } else {
            return -1;
        }
    }
}

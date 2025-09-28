package com.comix.scrapers.bedetheque.client.model.serie;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SeriePagination {

    private String currentPageNumber;
    private String nextPageUrl;
    private String totalPages;
    private String allGraphicNovelsInOnePageUrl;
}

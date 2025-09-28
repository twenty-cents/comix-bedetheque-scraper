package com.comix.scrapers.bedetheque.client.model.author;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"collaborationDetails"})
@ToString(exclude = {"collaborationDetails"})
public class Collaboration {

    private String type;
    private List<CollaborationDetails> collaborationDetails;
}

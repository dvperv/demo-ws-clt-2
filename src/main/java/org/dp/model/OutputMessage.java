package org.dp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor //needed for Jackson
@Getter
@Setter
public class OutputMessage {
    private String from;
    private String text;
    private String time;
}
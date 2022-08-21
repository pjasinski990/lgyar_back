package com.lgyar.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document
public class Envelope {
    private String categoryName;
    @JsonFormat(shape=JsonFormat.Shape.STRING)
    private BigDecimal limit;
    @JsonFormat(shape=JsonFormat.Shape.STRING)
    private BigDecimal spent;
}

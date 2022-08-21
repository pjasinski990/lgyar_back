package com.lgyar.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document
public class Transaction {
    private String category;
    @JsonFormat(shape=JsonFormat.Shape.STRING)
    private BigDecimal balanceDifference;

    @JsonFormat(pattern="dd.MM.yyyy HH:mm:ss")
    private LocalDateTime timestamp;
}

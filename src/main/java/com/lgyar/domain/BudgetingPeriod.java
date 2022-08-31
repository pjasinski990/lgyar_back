package com.lgyar.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document
public class BudgetingPeriod {
    private List<Envelope> envelopes;
    private List<Transaction> transactions;

    @JsonFormat(pattern="dd.MM.yyyy")
    private LocalDate startDate;
    @JsonFormat(pattern="dd.MM.yyyy")
    private LocalDate endDate;

    @JsonFormat(shape=JsonFormat.Shape.STRING)
    private BigDecimal availableMoney;
}

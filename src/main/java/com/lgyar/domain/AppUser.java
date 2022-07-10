package com.lgyar.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class AppUser {
    @Id
    private String username;
    private String passwordHash;
    private UserRole role;
    private BudgetingPeriod activePeriod;
    private List<BudgetingPeriod> previousPeriods;
}

package com.lgyar.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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

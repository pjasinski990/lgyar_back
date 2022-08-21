package com.lgyar.controllers;

import com.lgyar.domain.AppUser;
import com.lgyar.domain.BudgetingPeriod;
import com.lgyar.domain.Envelope;
import com.lgyar.domain.Transaction;
import com.lgyar.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping(value = "ap")
public class ActivePeriodController {

    private final UserRepository repository;

    @PostMapping(value = "create")
    public ResponseEntity<?> create(Authentication auth, @RequestBody List<Envelope> envelopes) {
        Optional<AppUser> u = repository.findById(auth.getName());
        if (u.isEmpty()) {
            return ResponseEntity.internalServerError().build();
        }

        AppUser user = u.get();
        BudgetingPeriod lastPeriod = user.getActivePeriod();
        if (lastPeriod != null) {
            user.getPreviousPeriods().add(lastPeriod);
        }

        LocalDate periodStart = LocalDate.now();
        LocalDate periodEnd = periodStart.plusMonths(1);
        BudgetingPeriod newPeriod = new BudgetingPeriod(envelopes, new ArrayList<>(), periodStart, periodEnd);
        user.setActivePeriod(newPeriod);
        repository.save(user);

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(new BudgetingPeriod());
    }
}

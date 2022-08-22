package com.lgyar.controllers;

import com.lgyar.domain.AppUser;
import com.lgyar.domain.BudgetingPeriod;
import com.lgyar.domain.Envelope;
import com.lgyar.domain.Transaction;
import com.lgyar.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
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
            List<BudgetingPeriod> previousPeriods = user.getPreviousPeriods();
            if (previousPeriods == null) {
                previousPeriods = new ArrayList<>();
            }
            previousPeriods.add(lastPeriod);
            user.setPreviousPeriods(previousPeriods);
        }

        LocalDate periodStart = LocalDate.now();
        LocalDate periodEnd = periodStart.plusMonths(1);
        BudgetingPeriod newPeriod = new BudgetingPeriod(envelopes, new ArrayList<>(), periodStart, periodEnd);
        user.setActivePeriod(newPeriod);
        repository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body(newPeriod);
    }

    @PostMapping(value = "archive")
    public ResponseEntity<?> archive(Authentication auth) {
        Optional<AppUser> u = repository.findById(auth.getName());
        if (u.isEmpty()) {
            return ResponseEntity.internalServerError().build();
        }
        AppUser user = u.get();

        BudgetingPeriod activePeriod = user.getActivePeriod();
        if (activePeriod == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        List<BudgetingPeriod> previousPeriods = user.getPreviousPeriods();
        if (previousPeriods == null) {
            previousPeriods = new ArrayList<>();
        }
        previousPeriods.add(activePeriod);
        user.setPreviousPeriods(previousPeriods);
        user.setActivePeriod(null);
        repository.save(user);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "new_transaction")
    public ResponseEntity<?> newTransaction(Authentication auth, @RequestBody Transaction newTransaction) {
        Optional<AppUser> u = repository.findById(auth.getName());
        if (u.isEmpty()) {
            return ResponseEntity.internalServerError().build();
        }
        AppUser user = u.get();

        BudgetingPeriod activePeriod = user.getActivePeriod();
        if (activePeriod == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        activePeriod.getTransactions().add(newTransaction);
        user.setActivePeriod(activePeriod);
        repository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body(newTransaction);
    }
}

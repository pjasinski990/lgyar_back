package com.lgyar.controllers;

import com.lgyar.domain.AppUser;
import com.lgyar.domain.BudgetingPeriod;
import com.lgyar.domain.Envelope;
import com.lgyar.domain.Transaction;
import com.lgyar.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping(value = "ap")
public class ActivePeriodController {

    private final UserRepository repository;

    private AppUser getUser(Authentication auth) {
        Optional<AppUser> u = repository.findById(auth.getName());
        if (u.isEmpty()) {
            throw new RuntimeException("No user with username " + auth.getName());
        }
        return u.get();
    }

    @PostMapping(value = "create")
    public ResponseEntity<?> create(Authentication auth, @RequestBody List<Envelope> envelopes) {
        AppUser user = getUser(auth);
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
        AppUser user = getUser(auth);
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

    @PostMapping(value = "add_transaction")
    public ResponseEntity<?> addTransaction(Authentication auth, @RequestBody Transaction newTransaction) {
        AppUser user = getUser(auth);
        BudgetingPeriod activePeriod = user.getActivePeriod();
        if (activePeriod == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        activePeriod.getTransactions().add(newTransaction);
        user.setActivePeriod(activePeriod);
        repository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body(newTransaction);
    }

    @PostMapping(value = "add_envelope")
    public ResponseEntity<?> addEnvelope(Authentication auth, @RequestBody Envelope target) {
        AppUser user = getUser(auth);
        BudgetingPeriod activePeriod = user.getActivePeriod();
        if (activePeriod == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        activePeriod.getEnvelopes().add(target);
        user.setActivePeriod(activePeriod);
        repository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body(target);
    }

    @PostMapping(value = "remove_envelope")
    public ResponseEntity<?> removeEnvelope(Authentication auth, @RequestBody Envelope target) {
        AppUser user = getUser(auth);
        BudgetingPeriod activePeriod = user.getActivePeriod();
        if (activePeriod == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        activePeriod.getEnvelopes().remove(target);
        user.setActivePeriod(activePeriod);
        repository.save(user);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(target);
    }

    @PostMapping(value = "update_envelope")
    public ResponseEntity<?> updateEnvelope(Authentication auth, @RequestBody Envelope target) {
        AppUser user = getUser(auth);
        BudgetingPeriod activePeriod = user.getActivePeriod();
        if (activePeriod == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        List<Envelope> envelopes = activePeriod.getEnvelopes();
        envelopes = envelopes.stream().filter(envelope ->
                !Objects.equals(envelope.getCategoryName(), target.getCategoryName())
        ).collect(Collectors.toList());
        activePeriod.setEnvelopes(envelopes);
        user.setActivePeriod(activePeriod);
        repository.save(user);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(target);
    }

    @PostMapping(value = "remove_transaction")
    public ResponseEntity<?> removeTransaction(Authentication auth, @RequestBody Transaction target) {
        AppUser user = getUser(auth);
        BudgetingPeriod activePeriod = user.getActivePeriod();
        if (activePeriod == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        List<Transaction> newTransactions = activePeriod.getTransactions()
                .stream()
                .filter(t -> !t.getTimestamp().equals(target.getTimestamp()))
                .collect(Collectors.toList());
        activePeriod.setTransactions(newTransactions);
        user.setActivePeriod(activePeriod);
        repository.save(user);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(target);
    }
}

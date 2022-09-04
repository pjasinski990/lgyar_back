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
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin(origins="*")
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

    private boolean hasPeriodWithStartingDate(AppUser user, LocalDate date) {
        List<BudgetingPeriod> previousPeriods = user.getPreviousPeriods();
        if (previousPeriods != null) {
            BudgetingPeriod existing = previousPeriods
                    .stream()
                    .filter(period -> date.equals(period.getStartDate()))
                    .findAny()
                    .orElse(null);
            return existing != null;
        }
        return false;
    }

    @GetMapping(value = "")
    public ResponseEntity<?> retrieve(Authentication auth, @RequestBody BudgetingPeriod newPeriod) {
        AppUser user = getUser(auth);
        BudgetingPeriod ap = user.getActivePeriod();
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(ap);
    }

    @PostMapping(value = "create")
    public ResponseEntity<?> create(Authentication auth, @RequestBody BudgetingPeriod newPeriod) {
        AppUser user = getUser(auth);
        BudgetingPeriod lastPeriod = user.getActivePeriod();
        if (lastPeriod != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        if (newPeriod.getStartDate() == null) {
            newPeriod.setStartDate(LocalDate.now());
        }
        if (newPeriod.getEndDate() == null) {
            newPeriod.setEndDate(newPeriod.getStartDate().plusMonths(1));
        }
        if (hasPeriodWithStartingDate(user, newPeriod.getStartDate())) {
            HashMap<String, String> body = new HashMap<>();
            body.put("error_message", "Budgeting period that starts on " + newPeriod.getStartDate() + " already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_JSON).body(body);
        }

        user.setActivePeriod(newPeriod);
        repository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body(newPeriod);
    }

    @PostMapping(value = "archive")
    public ResponseEntity<?> archive(Authentication auth) {
        AppUser user = getUser(auth);
        BudgetingPeriod activePeriod = user.getActivePeriod();
        if (activePeriod == null) {
            HashMap<String, String> body = new HashMap<>();
            body.put("error_message", "There is no active budgeting period");
            return ResponseEntity.status(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_JSON).body(body);
        }
        List<BudgetingPeriod> previousPeriods = user.getPreviousPeriods();
        if (previousPeriods == null) {
            previousPeriods = new ArrayList<>();
        }
        previousPeriods.add(activePeriod);
        user.setPreviousPeriods(previousPeriods);
        user.setActivePeriod(null);

        repository.save(user);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(activePeriod);
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

    @PostMapping(value = "edit_envelope")
    public ResponseEntity<?> editEnvelope(Authentication auth, @RequestBody Envelope target) {
        AppUser user = getUser(auth);
        BudgetingPeriod activePeriod = user.getActivePeriod();
        if (activePeriod == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        List<Envelope> envelopes = activePeriod.getEnvelopes();
        for (int i = 0; i < envelopes.size(); ++i) {
            if (envelopes.get(i).getCategoryName().equals(target.getCategoryName())) {
                envelopes.set(i, target);
            }
        }
        activePeriod.setEnvelopes(envelopes);
        user.setActivePeriod(activePeriod);
        repository.save(user);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(target);
    }

    @PostMapping(value = "remove_envelope")
    public ResponseEntity<?> removeEnvelope(Authentication auth, @RequestBody Envelope target) {
        AppUser user = getUser(auth);
        BudgetingPeriod activePeriod = user.getActivePeriod();
        if (activePeriod == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        List<Envelope> newEnvelopes = activePeriod.getEnvelopes()
                        .stream()
                        .filter(e -> !e.getCategoryName().equals(target.getCategoryName()))
                        .collect(Collectors.toList());
        activePeriod.setEnvelopes(newEnvelopes);
        user.setActivePeriod(activePeriod);
        repository.save(user);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(target);
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

    @PostMapping(value = "edit_available_money")
    public ResponseEntity<?> editAvailableMoney(Authentication auth, @RequestBody BigDecimal newValue) {
        AppUser user = getUser(auth);
        BudgetingPeriod activePeriod = user.getActivePeriod();
        if (activePeriod == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        user.getActivePeriod().setAvailableMoney(newValue);
        repository.save(user);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(newValue);
    }

    @PostMapping(value = "edit_start_date")
    public synchronized ResponseEntity<?> editStartDate(Authentication auth, @RequestBody LocalDate newDate) {
        AppUser user = getUser(auth);
        BudgetingPeriod activePeriod = user.getActivePeriod();
        if (activePeriod == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        if (hasPeriodWithStartingDate(user, newDate)) {
            HashMap<String, String> body = new HashMap<>();
            body.put("error_message", "Budgeting period that starts on " + newDate + " already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_JSON).body(body);
        }

        user.getActivePeriod().setStartDate(newDate);
        repository.save(user);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(newDate);
    }

    @PostMapping(value = "edit_end_date")
    public synchronized ResponseEntity<?> editEndDate(Authentication auth, @RequestBody LocalDate newDate) {
        AppUser user = getUser(auth);
        BudgetingPeriod activePeriod = user.getActivePeriod();
        if (activePeriod == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        user.getActivePeriod().setEndDate(newDate);
        repository.save(user);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(newDate);
    }
}

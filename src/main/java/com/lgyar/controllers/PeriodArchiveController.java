package com.lgyar.controllers;

import com.lgyar.domain.AppUser;
import com.lgyar.domain.BudgetingPeriod;
import com.lgyar.domain.Envelope;
import com.lgyar.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin(origins="*")
@Controller
@RequiredArgsConstructor
@RequestMapping(value = "archive")
public class PeriodArchiveController {

    private final UserRepository repository;

    private AppUser getUser(Authentication auth) {
        Optional<AppUser> u = repository.findById(auth.getName());
        if (u.isEmpty()) {
            throw new RuntimeException("No user with username " + auth.getName());
        }
        return u.get();
    }

    @GetMapping(value = "")
    public ResponseEntity<?> retrieve(Authentication auth) {
        AppUser user = getUser(auth);
        List<BudgetingPeriod> archive = user.getPreviousPeriods();
        if (archive == null) {
            archive = List.of();
        }
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(archive);
    }

    @PostMapping(value = "activate_period")
    public ResponseEntity<?> activatePeriod(Authentication auth, @RequestBody BudgetingPeriod target) {
        AppUser user = getUser(auth);
        if (user.getActivePeriod() != null) {
            HashMap<String, String> body = new HashMap<>();
            body.put("error_message", "There is already a budgeting period active - finish it before activating another");
            return ResponseEntity.status(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_JSON).body(body);
        }
        List<BudgetingPeriod> archive = user.getPreviousPeriods();
        if (archive == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        List<BudgetingPeriod> newArchive = user.getPreviousPeriods()
                .stream()
                .filter(p -> !p.getStartDate().equals(target.getStartDate()))
                .collect(Collectors.toList());
        user.setActivePeriod(target);
        user.setPreviousPeriods(newArchive);
        repository.save(user);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(target);
    }


    @PostMapping(value = "remove_period")
    public ResponseEntity<?> removePeriod(Authentication auth, @RequestBody BudgetingPeriod target) {
        AppUser user = getUser(auth);
        List<BudgetingPeriod> archive = user.getPreviousPeriods();
        if (archive == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        List<BudgetingPeriod> newArchive = user.getPreviousPeriods()
                .stream()
                .filter(p -> !p.getStartDate().equals(target.getStartDate()))
                .collect(Collectors.toList());
        user.setPreviousPeriods(newArchive);
        repository.save(user);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(target);
    }

}

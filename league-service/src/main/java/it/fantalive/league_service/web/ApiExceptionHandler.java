package it.fantalive.league_service.web;

import it.fantalive.league_service.exception.EventPublishingException;
import it.fantalive.league_service.exception.LeagueNotFoundException;
import it.fantalive.league_service.exception.NotFoundException;
import it.fantalive.league_service.exception.RuleViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(LeagueNotFoundException.class)
    public ProblemDetail notFound(LeagueNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(RuleViolationException.class)
    public ProblemDetail ruleViolation(RuleViolationException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ProblemDetail conflict(OptimisticLockingFailureException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "La lega è stata modificata da un'altra richiesta: riprova");
    }

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail notFoundGeneric(NotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(EventPublishingException.class)
    public ProblemDetail publishingFailed(EventPublishingException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }
}
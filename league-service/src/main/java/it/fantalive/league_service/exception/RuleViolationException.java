package it.fantalive.league_service.exception;

public class RuleViolationException extends RuntimeException {
    public RuleViolationException(String message) { super(message); }
}
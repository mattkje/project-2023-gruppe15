package no.ntnu.message;

/**
 * An error message after a command execution which failed.
 */
public record ErrorMessage(String message) implements Message {

}

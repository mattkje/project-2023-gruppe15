package no.gruppe15.greenhouse;

/**
 * A command sent from the client to the server (from remote to TV).
 */
public abstract class Command {
  /**
   * Execute the command.
   *
   * @return The message which contains the output of the command
   */
  public abstract void execute();
}
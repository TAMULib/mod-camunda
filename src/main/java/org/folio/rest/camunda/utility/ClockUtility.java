package org.folio.rest.camunda.utility;

import java.time.Clock;
import java.time.ZonedDateTime;
import org.springframework.stereotype.Component;

/**
 * A utility for getting the current date and time in a way that is safely mockable.
 *
 * This utility must be used for all attempts to get the current time.
 */
@Component
public class ClockUtility {

  private final Clock clock;

  /**
   * Class constructor.
   *
   * @param clock The system clock to use.
   */
  public ClockUtility(Clock clock) {

    this.clock = clock;
  }

  /**
   * Get the current zoned date time using the loaded clock.
   *
   * @return The current zoned date time.
   */
  public ZonedDateTime now() {

    return ZonedDateTime.now(clock);
  }

}

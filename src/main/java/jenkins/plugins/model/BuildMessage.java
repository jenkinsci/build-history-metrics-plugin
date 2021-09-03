package jenkins.plugins.model;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import lombok.Getter;

/**
 * BuildMessage class that contains the data needed to calculate the Mean Time
 * Metrics.
 *
 * @author mcgin
 */
@Getter
public class BuildMessage
    implements Comparator<BuildMessage>,
        Comparable<BuildMessage>,
        Serializable {

  /** The start time of the {@link Build}. */
  private final long startTime;
  /** The result of the {@link Build}. */
  private final String result;
  /** The duration of the {@link Build}. */
  private final long duration;
  /** The buildNumber of the {@link Build}. */
  private final long buildNumber;

  /**
   * BuildMessage constructor.
   *
   * @param pBuildNumber The buildNumber of the {@link Build}.
   * @param pStartTime The start time of the {@link Build}.
   * @param pDuration The duration of the {@link Build}.
   * @param pResult The result of the {@link Build}.
   */
  public BuildMessage(
      final long pBuildNumber,
      final long pStartTime,
      final long pDuration,
      final String pResult) {
    this.startTime = pStartTime;
    this.result = pResult;
    this.duration = pDuration;
    this.buildNumber = pBuildNumber;
  }

  /** {@inheritDoc} */
  @Override
  public final int compare(
      final BuildMessage message1, final BuildMessage message2) {
    return (int) (message1.getBuildNumber() - message2.getBuildNumber());
  }

  /** {@inheritDoc} */
  @Override
  public final int compareTo(final BuildMessage message) {
    return compare(this, message);
  }

  /** {@inheritDoc} */
  @Override
  public final String toString() {
    return buildNumber + "\t" + result;
  }

  /** {@inheritDoc} */
  @Override
  public final boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BuildMessage that = (BuildMessage) o;
    return buildNumber == that.buildNumber;
  }

  /** {@inheritDoc} */
  @Override
  public final int hashCode() {
    return Objects.hash(buildNumber);
  }
}

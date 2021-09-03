package jenkins.plugins.mttr;

import hudson.model.Job;
import java.io.IOException;

/**
 * The Results Column Interface.
 *
 * @author mcgin
 */
public interface ResultColumn {
  /**
   * Gets the results for a {@link Job}.
   *
   * @param job The job that we want to get the results for.
   * @return The results as a {@link String}.
   * @throws IOException results are in a file so we may have an exception when
   *     reading.
   */
  String getResult(final Job job) throws IOException;

  /**
   * Gets the results graph for a {@link Job}.
   *
   * @param job The job that we want to get the results for.
   * @return The results graph as a {@link String}.
   * @throws IOException results are in a file so we may have an exception when
   *     reading.
   */
  String getGraph(final Job job) throws IOException;
}

Jenkins Build History Metrics Plugin
======================================

[![Build Status](https://ci.jenkins.io/job/Plugins/job/build-history-metrics-plugin/job/main/badge/icon)](https://ci.jenkins.io/job/Plugins/job/build-history-metrics-plugin/job/main/)
[![Coverage](https://ci.jenkins.io/job/Plugins/job/build-history-metrics-plugin/job/main/badge/icon?status=${instructionCoverage}&subject=coverage&color=${colorInstructionCoverage})](https://ci.jenkins.io/job/Plugins/job/build-history-metrics-plugin/job/main)
[![LOC](https://ci.jenkins.io/job/Plugins/job/build-history-metrics-plugin/job/main/badge/icon?job=test&status=${lineOfCode}&subject=line%20of%20code&color=blue)](https://ci.jenkins.io/job/Plugins/job/build-history-metrics-plugin/job/main)
![Contributors](https://img.shields.io/github/contributors/jenkinsci/build-history-metrics-plugin.svg?color=blue)
[![GitHub release](https://img.shields.io/github/release/jenkinsci/build-history-metrics-plugin.svg?label=changelog)](https://github.com/jenkinsci/build-history-metrics-plugin/releases/latest)
[![Jenkins Plugin Installs](https://img.shields.io/jenkins/plugin/i/build-history-metrics-plugin.svg?color=blue)](https://plugins.jenkins.io/build-history-metrics-plugin)

This plugin calculates the following metrics for all of your builds once
installed

The plugin doesn't support pipeline jobs (contributions are welcome).

-   Mean Time To Failure (MTTF)
-   Mean Time To Recovery (MTTR)
-   Standard Deviation of Build Times

The calculated metrics are displayed in a table on each jobs page
showing the metric for the last 7 days, last 30 days and all time.  The
table looks something like this:

![](docs/table.png)

The plugin also add new column definitions

![](docs/columns.png)

## Changelog

Changelog is now published on GitHub releases page

### 1.2

-   Standard deviation for build times is now calculated and displayed
-   Some styling changes for the table to make it look a bit better

### 1.1

-   First stable release that is a fork of an old plugin, refactored
    with more tests and extended to include both MTTF and MTTR metrics

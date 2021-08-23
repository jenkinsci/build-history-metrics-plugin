Jenkins Build History Metrics Plugin
======================================

[![Build Status](https://ci.jenkins.io/job/Plugins/job/configuration-as-code-plugin/job/master/badge/icon)](https://ci.jenkins.io/job/Plugins/job/configuration-as-code-plugin/job/master/)

This plugin calculates the following metrics for all of your builds once
installed:

-   Mean Time To Failure (MTTF)
-   Mean Time To Recovery (MTTR)
-   Standard Deviation of Build Times

The calculated metrics are displayed in a table on each jobs page
showing the metric for the last 7 days, last 30 days and all time.  The
table looks something like this:  
![](https://wiki.jenkins.io/download/attachments/74417004/build-history-metrics-plugin-table.jpg?version=1&modificationDate=1433604472000&api=v2)

## Changelog

### 1.2

-   Standard deviation for build times is now calculated and displayed
-   Some styling changes for the table to make it look a bit better

### 1.1

-   First stable release that is a fork of an old plugin, refactored
    with more tests and extended to include both MTTF and MTTR metrics


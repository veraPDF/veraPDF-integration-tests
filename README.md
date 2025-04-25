veraPDF-integration-tests
=========================

[![Build Status](https://jenkins.openpreservation.org/job/veraPDF/job/1.28/job/integration-tests-jakarta/badge/icon)](https://jenkins.openpreservation.org/job/veraPDF/job/1.28/job/integration-tests-jakarta/ "OPF Jenkins")
[![CodeCov Coverage](https://img.shields.io/codecov/c/github/veraPDF/veraPDF-integration-tests.svg)](https://codecov.io/gh/veraPDF/veraPDF-integration-tests/ "CodeCov coverage")
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/8d54ee7467f14bf5844b91081981f6ee)](https://app.codacy.com/gh/veraPDF/veraPDF-integration-tests/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade "Codacy coverage")

[![GitHub issues](https://img.shields.io/github/issues/veraPDF/veraPDF-library.svg)](https://github.com/veraPDF/veraPDF-library/issues "Open issues on GitHub")
[![GitHub issues](https://img.shields.io/github/issues-closed/veraPDF/veraPDF-library.svg)](https://github.com/veraPDF/veraPDF-library/issues?q=is%3Aissue+is%3Aclosed "Closed issues on GitHub")
[![GitHub issues](https://img.shields.io/github/issues-pr/veraPDF/veraPDF-integration-tests.svg)](https://github.com/veraPDF/veraPDF-integration-tests/pulls "Open pull requests on GitHub")
[![GitHub issues](https://img.shields.io/github/issues-pr-closed/veraPDF/veraPDF-integration-tests.svg)](https://github.com/veraPDF/veraPDF-integration-tests/pulls?q=is%3Apr+is%3Aclosed "Closed pull requests on GitHub")

Licensing
---------
The veraPDF PDF/A Validation Library is dual-licensed, see:

 - [GPLv3+](LICENSE.GPL "GNU General Public License, version 3")
 - [MPLv2+](LICENSE.MPL "Mozilla Public License, version 2.0")

Documentation
-------------
See the [veraPDF documentation site](http://docs.verapdf.org/).

Quick Start
-----------
### Pre-requisites

In order to build the library you'll need:

 * Java 11, 17 or 21, which can be downloaded [from Oracle](https://www.oracle.com/technetwork/java/javase/downloads/index.html), or for Linux users [OpenJDK](https://openjdk.java.net/install/index.html).
 * [Maven v3+](https://maven.apache.org/)

Life will be easier if you also use [Git](https://git-scm.com/) to obtain and manage the source.

### Please Read

Here are some of the testing tools we use, it's currently a project used by
the veraPDF development team. Instructions are currently sparse but will be
improved.

### Producing HTML test reports

The project's Maven build produces HTML reports, currently to `stdout` as well
as a set of files. To checkout the project and produce the test files locally:

    git clone https://github.com/veraPDF/veraPDF-integration-tests.git
    cd veraPDF-integration-tests
    mvn clean install
    ls target/test-results/

    BFO-2b          veraPDF-1a      veraPDF-2b      veraPDF-4       veraPDF-ua1
    Isartor-1b      veraPDF-1b      veraPDF-2u      veraPDF-4e      index.html
    TWG-0           veraPDF-2a      veraPDF-3b      veraPDF-4f

`index.html` can be opened in a browser and show the test results in a table with
green cells for successful tests and red cells for failed tests.

### Running regression tests

The project's Maven build generates .jar files that run regression tests for PDF and WCAG validation. 

To run regression tests after project's Maven build:

    cd veraPDF-pdf-regression-tests/target/
    java -jar veraPDF-pdf-regression-tests-${project.version}.jar

    cd ../../veraPDF-wcag-regression-tests/target/
    java -jar veraPDF-wcag-regression-tests-${project.version}.jar

Where `${project.version}` is the last development or release version. 
Regression test results will be produced to `stdout`.
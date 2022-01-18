veraPDF-integration-tests
=========================

[![Build Status](https://travis-ci.org/veraPDF/veraPDF-integration-tests.svg?branch=rel/1.20)](https://travis-ci.org/veraPDF/integration-tests/ "Travis-CI")
[![Build Status](http://jenkins.openpreservation.org/buildStatus/icon?job=veraPDF-integration-tests)](http://jenkins.openpreservation.org/job/veraPDF-integration-tests/ "OPF Jenkins Release")
[![CodeCov Coverage](https://img.shields.io/codecov/c/github/veraPDF/veraPDF-integration-tests.svg)](https://codecov.io/gh/veraPDF/veraPDF-integration-tests/ "CodeCov coverage")
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/41570ba00d614d4083bf249a1d6c852e)](https://www.codacy.com/app/carlwilson/veraPDF-integration-tests?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=veraPDF/veraPDF-integration-tests&amp;utm_campaign=Badge_Grade)

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

 * Java 7, which can be downloaded [from Oracle](http://www.oracle.com/technetwork/java/javase/downloads/index.html), or for Linux users [OpenJDK](http://openjdk.java.net/install/index.html).
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
    ls -alh target/*.html

    -rw-rw-r-- 1 cfw cfw 3.7K Apr 26 12:51 target/BFO-corpus-pdf2b.html
    -rw-rw-r-- 1 cfw cfw  14K Apr 26 12:51 target/Isartor-corpus-pdf1b.html
    -rw-rw-r-- 1 cfw cfw  23K Apr 26 12:51 target/veraPDF-corpus-pdf1b.html

These can be opened in a browser and show the test results in a table with
green cells for successful tests and red cells for failed tests.

veraPDF-integration-tests
=========================

Please Read
-----------
Here are some of the testing tools we use, it's currently a project used by
the veraPDF development team. Instructions are currently sparse but will be
improved.

Producing HTML test reports
---------------------------
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

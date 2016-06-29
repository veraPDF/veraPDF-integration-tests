/**
 * 
 */
package org.verapdf.integration;

import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.qa.TestCorpus;
import org.verapdf.pdfa.qa.ZipBackedTestCorpus;

import java.io.*;
import java.net.URL;

/**
 * @author <a href="mailto:carl@openpreservation.org">Carl Wilson</a>
 *
 */
public final class CorpusManager {
    // Reference to corpus zip temp file
    private static File VERA_CORPUS_ZIP_FILE = null;
    private static File ISARTOR_CORPUS_ZIP_FILE = null;
    private static File BFO_CORPUS_ZIP_FILE = null;
    private static TestCorpus VERA_1A_CORPUS = null;
    private static TestCorpus VERA_1B_CORPUS = null;
    private static TestCorpus VERA_2B_CORPUS = null;
    private static TestCorpus VERA_2U_CORPUS = null;
    private static TestCorpus VERA_3B_CORPUS = null;
    private static TestCorpus ISARTOR_CORPUS = null;
    private static TestCorpus BFO_CORPUS = null;

    /**
     * @return a TestCorpus set up from the downloaded verPDF test corpus zip file
     * @throws IOException if an error occurs downloading or parsing the corpus zip file
     */
    public static TestCorpus getVera1ACorpus() throws IOException {
        if (VERA_1A_CORPUS == null) {
            URL corpusURL = new URL(
                    "https://github.com/veraPDF/veraPDF-corpus/archive/staging.zip");
            if ((VERA_CORPUS_ZIP_FILE == null) || (!VERA_CORPUS_ZIP_FILE.exists())) {
            	VERA_CORPUS_ZIP_FILE = createTempFileFromUrl(corpusURL, "veraCorpus");
            }
            VERA_1A_CORPUS = ZipBackedTestCorpus.fromZipSource("veraPDF-1a-corpus", "Synthetic test files for PDF/A validation.",
                    VERA_CORPUS_ZIP_FILE, PDFAFlavour.PDFA_1_A);
        }
        return VERA_1A_CORPUS;
    }

    /**
     * @return a TestCorpus set up from the downloaded verPDF test corpus zip file
     * @throws IOException if an error occurs downloading or parsing the corpus zip file
     */
    public static TestCorpus getVera1BCorpus() throws IOException {
        if (VERA_1B_CORPUS == null) {
            URL corpusURL = new URL(
                    "https://github.com/veraPDF/veraPDF-corpus/archive/staging.zip");
            if ((VERA_CORPUS_ZIP_FILE == null) || (!VERA_CORPUS_ZIP_FILE.exists())) {
            	VERA_CORPUS_ZIP_FILE = createTempFileFromUrl(corpusURL, "veraCorpus");
            }
            VERA_1B_CORPUS = ZipBackedTestCorpus.fromZipSource("veraPDF-1b-corpus", "Synthetic test files for PDF/A validation.",
                    VERA_CORPUS_ZIP_FILE, PDFAFlavour.PDFA_1_B);
        }
        return VERA_1B_CORPUS;
    }

    public static TestCorpus getVera2BCorpus() throws IOException {
        if (VERA_2B_CORPUS == null) {
            URL corpusURL = new URL(
                    "https://github.com/veraPDF/veraPDF-corpus/archive/staging.zip");
            if ((VERA_CORPUS_ZIP_FILE == null) || (!VERA_CORPUS_ZIP_FILE.exists())) {
            	VERA_CORPUS_ZIP_FILE = createTempFileFromUrl(corpusURL, "veraCorpus");
            }
            VERA_2B_CORPUS = ZipBackedTestCorpus.fromZipSource("veraPDF-2b-corpus", "Synthetic test files for PDF/A validation.",
                    VERA_CORPUS_ZIP_FILE, PDFAFlavour.PDFA_2_B);
        }
        return VERA_2B_CORPUS;
    }

    public static TestCorpus getVera2UCorpus() throws IOException {
        if (VERA_2U_CORPUS == null) {
            URL corpusURL = new URL(
                    "https://github.com/veraPDF/veraPDF-corpus/archive/staging.zip");
            if ((VERA_CORPUS_ZIP_FILE == null) || (!VERA_CORPUS_ZIP_FILE.exists())) {
            	VERA_CORPUS_ZIP_FILE = createTempFileFromUrl(corpusURL, "veraCorpus");
            }
            VERA_2U_CORPUS = ZipBackedTestCorpus.fromZipSource("veraPDF-2u-corpus", "Synthetic test files for PDF/A validation.",
                    VERA_CORPUS_ZIP_FILE, PDFAFlavour.PDFA_2_U);
        }
        return VERA_2U_CORPUS;
    }

    public static TestCorpus getVera3BCorpus() throws IOException {
        if (VERA_3B_CORPUS == null) {
            URL corpusURL = new URL(
                    "https://github.com/veraPDF/veraPDF-corpus/archive/staging.zip");
            if ((VERA_CORPUS_ZIP_FILE == null) || (!VERA_CORPUS_ZIP_FILE.exists())) {
            	VERA_CORPUS_ZIP_FILE = createTempFileFromUrl(corpusURL, "veraCorpus");
            }
            VERA_3B_CORPUS = ZipBackedTestCorpus.fromZipSource("veraPDF-3b-corpus", "Synthetic test files for PDF/A validation.",
                    VERA_CORPUS_ZIP_FILE, PDFAFlavour.PDFA_3_B);
        }
        return VERA_3B_CORPUS;
    }

    /**
     * @return a TestCorpus set up from the downloaded Isartor test corpus zip file
     * @throws IOException if an error occurs downloading or parsing the corpus zip file
     */
    public static TestCorpus getIsartorCorpus() throws IOException {
        if (ISARTOR_CORPUS_ZIP_FILE == null) {
            URL corpusURL = new URL(
                    "http://www.pdfa.org/wp-content/uploads/2011/08/isartor-pdfa-2008-08-13.zip");
            ISARTOR_CORPUS_ZIP_FILE = createTempFileFromUrl(corpusURL, "isartorCorpus");
            ISARTOR_CORPUS = ZipBackedTestCorpus.fromZipSource("Isartor-corpus", "Synthetic test files for PDF/A validation.",
                    ISARTOR_CORPUS_ZIP_FILE, null);
        }
        return ISARTOR_CORPUS;
    }

    /**
     * @return a TestCorpus set up from the downloaded Isartor test corpus zip file
     * @throws IOException if an error occurs downloading or parsing the corpus zip file
     */
    public static TestCorpus getBFOCorpus() throws IOException {
        if (BFO_CORPUS_ZIP_FILE == null) {
            URL corpusURL = new URL(
                    "https://github.com/bfosupport/pdfa-testsuite/archive/master.zip");
            BFO_CORPUS_ZIP_FILE = createTempFileFromUrl(corpusURL, "bfoCorpus");
            BFO_CORPUS = ZipBackedTestCorpus.fromZipSource("BFO-corpus", "Synthetic test files for PDF/A validation.",
                    BFO_CORPUS_ZIP_FILE, null);
        }
        return BFO_CORPUS;
    }

    private static File createTempFileFromUrl(final URL sourceUrl,
            final String tempPrefix) throws IOException {
        File tempFile = File.createTempFile(tempPrefix, "zip");
        try (OutputStream output = new FileOutputStream(tempFile);
                InputStream corpusInput = sourceUrl.openStream();) {
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = corpusInput.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        }
        return tempFile;
    }
}

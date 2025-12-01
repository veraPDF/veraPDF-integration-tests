package org.verapdf.wcag.regression.tests;

import org.junit.Assert;
import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.qa.FailedPolicyCheck;
import org.verapdf.pdfa.qa.RegressionTestingHelper;
import org.verapdf.pdfa.validation.profiles.Profiles;
import org.verapdf.pdfa.validation.profiles.ValidationProfile;

import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class WCAGRegressionTestUtils {
    private static final String WCAG_FOLDER = "https://github.com/veraPDF/veraPDF-validation-profiles/raw/integration/PDF_UA/";
    private static final String WCAG_PROFILE_URL = WCAG_FOLDER + "WCAG-2-2-Complete.xml";
    private static final String WCAG_2_0_PROFILE_URL = WCAG_FOLDER + "WCAG-2-2-Complete-PDF20.xml";
    private static final EnumMap<PDFAFlavour, Set<String>> filesByFlavour = new EnumMap<>(PDFAFlavour.class);
    private static final Map<PDFAFlavour, String> map = new HashMap<>();
    
    static {
        map.put(PDFAFlavour.WCAG_2_2_HUMAN, WCAG_PROFILE_URL);
        map. put(PDFAFlavour.WCAG_2_2_PDF_2_0_HUMAN, WCAG_2_0_PROFILE_URL);
    }

    public static void main(String[] args) {
        test();
    }

    public static void test() {
        try {
            RegressionTestingHelper helper = new RegressionTestingHelper(true);
            for (PDFAFlavour flavour : map.keySet()) {
                Set<String> toAdd = helper.getPdfFileNames().stream().filter(s -> checkWCAGFlavour(s, flavour)).collect(Collectors.toSet());
                filesByFlavour.put(flavour, toAdd);
            }
            Map<String, List<FailedPolicyCheck>> failedFiles = new HashMap<>();
            for (Map.Entry<PDFAFlavour, String> entry : map.entrySet()) {
                ValidationProfile customProfile;
                try (InputStream is = (new URL(entry.getValue())).openStream()) {
                    customProfile = Profiles.profileFromXml(is);
                }
                helper.getFailedPolicyComplianceFiles(failedFiles, null, customProfile, filesByFlavour.get(entry.getKey()));
            }
            RegressionTestingHelper.printResult(failedFiles);
            Assert.assertEquals(0, failedFiles.size());
        } catch (IOException | JAXBException e) {
            Assert.fail("Some tests are fallen due to an error");
            e.printStackTrace();
        }
    }

    private static boolean checkWCAGFlavour(final String item, final PDFAFlavour flavour) {
        if (flavour == PDFAFlavour.WCAG_2_2_HUMAN) {
            return item.contains("1.7");
        }
        if (flavour == PDFAFlavour.WCAG_2_2_PDF_2_0_HUMAN) {
            return item.contains("2.0");
        }
        return false;
    }
}

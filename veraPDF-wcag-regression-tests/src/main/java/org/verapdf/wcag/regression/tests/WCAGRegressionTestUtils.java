package org.verapdf.wcag.regression.tests;

import org.junit.Assert;
import org.verapdf.pdfa.qa.FailedPolicyCheck;
import org.verapdf.pdfa.qa.RegressionTestingHelper;
import org.verapdf.pdfa.validation.profiles.Profiles;
import org.verapdf.pdfa.validation.profiles.ValidationProfile;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WCAGRegressionTestUtils {
    private static final String wcagProfileUrl = "https://github.com/veraPDF/veraPDF-validation-profiles/raw/rc/1.28/PDF_UA/WCAG-2-2-Complete.xml";

    public static void main(String[] args) {
        test();
    }

    public static void test() {
        try {
            RegressionTestingHelper helper = new RegressionTestingHelper(true);
            ValidationProfile customProfile;
            try (InputStream is = (new URL(wcagProfileUrl)).openStream()) {
                customProfile = Profiles.profileFromXml(is);
            }
            Map<String, List<FailedPolicyCheck>> failedFiles = new HashMap<>();
            helper.getFailedPolicyComplianceFiles(failedFiles, null, customProfile, helper.getPdfFileNames());
            RegressionTestingHelper.printResult(failedFiles);
            Assert.assertEquals(0, failedFiles.size());
        } catch (IOException | JAXBException e) {
            Assert.fail("Some tests are fallen due to an error");
            e.printStackTrace();
        }
    }
}

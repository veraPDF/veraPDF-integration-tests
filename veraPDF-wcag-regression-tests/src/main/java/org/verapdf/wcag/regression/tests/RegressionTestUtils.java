package org.verapdf.wcag.regression.tests;

import org.junit.Assert;
import org.verapdf.pdfa.qa.RegressionTestingHelper;
import org.verapdf.pdfa.validation.profiles.Profiles;
import org.verapdf.pdfa.validation.profiles.ValidationProfile;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class RegressionTestUtils {
    private static final String wcagProfileUrl = "https://github.com/veraPDF/veraPDF-validation-profiles/raw/integration/PDF_UA/WCAG-21-Complete.xml";

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
            Assert.assertFalse(RegressionTestingHelper.totalFailedPolicyJobsCount(helper
                    .getFailedPolicyComplianceFiles(null, customProfile, helper.getPdfFileNames())) > 0);
        } catch (IOException | JAXBException e) {
            Assert.fail("Some tests are fallen due to an error");
            e.printStackTrace();
        }
    }
}

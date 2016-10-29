/**
 * 
 */
package org.verapdf.pdfa.validators;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.verapdf.pdfa.qa.GitHubBackedProfileDirectory;
import org.verapdf.pdfa.validation.profiles.Rule;
import org.verapdf.pdfa.validation.profiles.ValidationProfile;

/**
 * @author <a href="mailto:carl@openpreservation.org">Carl Wilson</a>
 *
 */
@SuppressWarnings("static-method")
public class ProfilesTest {
    private static final org.verapdf.pdfa.validation.profiles.ProfileDirectory INTEGRATION_PROFILES = GitHubBackedProfileDirectory.fromBranch("integration");

    /**
     * Test method for {@link org.verapdf.pdfa.validation.ValidationProfileImpl#getPDFAFlavour()}.
     */
    @Test
    public final void testGetPDFAFlavour() {
        for (ValidationProfile profile: INTEGRATION_PROFILES.getValidationProfiles()) {
            for (Rule rule : profile.getRules()) {
                assertTrue("Profile=" + profile.getPDFAFlavour().getPart() + ", rule=" + rule.getRuleId().getSpecification(), rule.getRuleId().getSpecification() == profile.getPDFAFlavour().getPart());
            }
        }
    }

}

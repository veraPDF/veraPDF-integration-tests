/**
 * This file is part of veraPDF Quality Assurance, a module of the veraPDF project.
 * Copyright (c) 2015, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF Quality Assurance is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF Quality Assurance as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * veraPDF Quality Assurance as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
/**
 * 
 */
package org.verapdf.pdfa.validation.validators.test;

import org.junit.BeforeClass;
import org.verapdf.core.EncryptedPdfException;
import org.verapdf.core.ModelParsingException;
import org.verapdf.core.ValidationException;
import org.verapdf.core.XmlSerialiser;
import org.verapdf.model.ModelParser;
import org.verapdf.pdfa.Foundries;
import org.verapdf.pdfa.PDFAParser;
import org.verapdf.pdfa.PDFAValidator;
import org.verapdf.pdfa.validation.validators.ValidatorBuilder;
import org.verapdf.pdfbox.foundry.PdfBoxFoundryProvider;
import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.qa.AbstractTestCorpus.Corpus;
import org.verapdf.pdfa.qa.CorpusManager;
import org.verapdf.pdfa.qa.CorpusSampler;
import org.verapdf.pdfa.qa.GitHubBackedProfileDirectory;
import org.verapdf.pdfa.qa.TestCorpus;
import org.verapdf.pdfa.results.TestAssertion;
import org.verapdf.pdfa.results.TestAssertion.Status;
import org.verapdf.pdfa.results.ValidationResult;
import org.verapdf.pdfa.results.ValidationResults;
import org.verapdf.pdfa.validation.profiles.ProfileDirectory;
import org.verapdf.pdfa.validation.profiles.ValidationProfile;

import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:carl@openpreservation.org">Carl Wilson</a>
 *
 */
@SuppressWarnings("static-method")
public class ValidatorTest {
    private static final ProfileDirectory PROFILES = GitHubBackedProfileDirectory.fromBranch("rc/1.24");

    @BeforeClass
    public static final void SetUp() {
    	PdfBoxFoundryProvider.initialise();
    }
    /**
     * Test method for
     * {@link org.verapdf.pdfa.validators.BaseValidator#getProfile()}.
     */
//    @Test
    public final void testGetProfile() {
        for (ValidationProfile profile : PROFILES.getValidationProfiles()) {
            PDFAValidator validator = Foundries.defaultInstance().createValidator(profile, false);
            assertTrue(profile.equals(validator.getProfile()));
        }
    }

    /**
     * Test method for
     * {@link org.verapdf.pdfa.validators.BaseValidator#validate(org.verapdf.pdfa.ValidationModelParser)}
     * .
     * 
     * @throws IOException
     * @throws JAXBException
     * @throws ModelParsingException 
     */
 //   @Test
    public final void testValidateValidationConsistency() throws IOException,
            JAXBException, ModelParsingException, EncryptedPdfException {
        // Grab a random sample of 20 corpus files
        TestCorpus veraCorpus = CorpusManager.corpusByFlavourAndType(PDFAFlavour.PDFA_1_B, Corpus.VERA);
        Set<String> sample = CorpusSampler.randomSample(veraCorpus, 20);
        // / Cycle through sample
        for (String itemName : sample) {
            // Try all profiles
            for (ValidationProfile profile : PROFILES.getValidationProfiles()) {
                // Create a validator for profile
                PDFAValidator validator = Foundries.defaultInstance().createValidator(profile,
                        false);
                Set<ValidationResult> results = new HashSet<>();
                // Validate a fresh model instance and add the result to the set
                for (int index = 0; index < 2; index++) {
                    try (PDFAParser parser = ModelParser.createModelWithFlavour(
                            veraCorpus.getItemStream(itemName), profile.getPDFAFlavour())) {
                        ValidationResult result = validator.validate(parser);
                        results.add(result);
                    } catch (ValidationException e) {
                        checkValidationException(itemName, e);
                        results.add(ValidationResults.defaultResult());
                    }
                }
                assertEquals(resultsMessage(veraCorpus.getDetails().getName(),
                        itemName, profile.getPDFAFlavour().toString(),
                        results), 1, results.size());
            }
        }
    }

    @SuppressWarnings("javadoc")
   // @Test
    public void testFailFastValidator() throws IOException, JAXBException, ModelParsingException, EncryptedPdfException {
        // Grab a random sample of 20 corpus files
        TestCorpus veraCorpus = CorpusManager.corpusByFlavourAndType(PDFAFlavour.PDFA_1_B, Corpus.VERA);
        Set<String> sample = CorpusSampler.randomSample(veraCorpus, 20);
        // / Cycle through sample
        for (String itemName : sample) {
            // Try all profiles
            for (ValidationProfile profile : PROFILES.getValidationProfiles()) {
                // Create a validator for the profile and get a result with no
                // failures
                PDFAValidator validator = Foundries.defaultInstance().createValidator(profile,
                        false);
                ValidationResult result = ValidationResults.defaultResult();
                // Validate a fresh model instance and add the result to the set
                try (PDFAParser parser = ModelParser.createModelWithFlavour(
                        veraCorpus.getItemStream(itemName), profile.getPDFAFlavour())) {
                    result = validator.validate(parser);
                } catch (ValidationException e) {
                    checkValidationException(itemName, e);
                    continue;
                }
                int failedMax = result.getTestAssertions().size() + 1;
                // Set up a loop to restrict failures
                for (int index = failedMax; index > 0; index--) {
                    PDFAValidator fastFailValidator = new ValidatorBuilder().profile(profile).maxFails(index).build();
                    ValidationResult failFastResult = ValidationResults.defaultResult();
                    try (ModelParser parser = ModelParser.createModelWithFlavour(
                            veraCorpus.getItemStream(itemName), profile.getPDFAFlavour())) {
                        failFastResult = fastFailValidator.validate(parser);
                    } catch (ValidationException e) {
                        checkValidationException(itemName, e);
                        continue;
                    }
                    if (index == failedMax) {
                        assertTrue(resultsComparisonMessage(veraCorpus
                                        .getDetails().getName(), itemName,
                                        profile.getPDFAFlavour().toString(),
                                        result, failFastResult),
                                result.equals(failFastResult));
                    } else if ((index == (failedMax -1)) && (getMaxFailureOrdinal(result) == result.getTotalAssertions())) {
                        assertTrue(resultsComparisonMessage(veraCorpus
                                .getDetails().getName(), itemName,
                                profile.getPDFAFlavour().toString(),
                                result, failFastResult),
                        result.equals(failFastResult));
                    } else if (index < failedMax) {
                        assertFalse(resultsComparisonMessage(veraCorpus
                                .getDetails().getName(), itemName,
                                profile.getPDFAFlavour().toString(),
                                result, failFastResult), result.equals(failFastResult));

                    }
                }
            }
        }
    }

    /**
     * TODO: Sort the validator consistency issues
     * 
     * @throws IOException
     * @throws ValidationException
     * @throws JAXBException
     * @throws ModelParsingException 
     */
    //@Test
    public void testModelConsistency() throws IOException, ValidationException,
            JAXBException, ModelParsingException, EncryptedPdfException {
        // Grab a random sample of 10 corpus files
        TestCorpus veraCorpus = CorpusManager.corpusByFlavourAndType(PDFAFlavour.PDFA_1_B, Corpus.VERA);
        Set<String> sample = CorpusSampler.randomSample(veraCorpus, 10);

        // Cycle through all available profile on GitHub
        for (ValidationProfile profile : PROFILES.getValidationProfiles()) {
            for (String itemName : sample) {
                // Create fresh validators for each sample item
                PDFAValidator validator = Foundries.defaultInstance().createValidator(profile,
                        false);
                PDFAValidator checkValidator = Foundries.defaultInstance().createValidator(
                        profile, false);
                // Create a new model parser instance
                try (PDFAParser parser = ModelParser.createModelWithFlavour(
                        veraCorpus.getItemStream(itemName), profile.getPDFAFlavour())) {
                    // Validate model with fresh validator
                    ValidationResult firstResult = validator.validate(parser);
                    // Validate same model with second fresh validator instance
                    ValidationResult checkResult = checkValidator
                            .validate(parser);
                    // Validate model with first validator again
                    ValidationResult secondResult = validator.validate(parser);

                    // The results of the two separate validators should be the
                    // same (this works)
                    assertTrue(
                            resultsComparisonMessage(veraCorpus.getDetails()
                                    .getName(), itemName, profile
                                    .getPDFAFlavour().toString(), firstResult,
                                    secondResult),
                            checkResult.equals(secondResult));
                    // The results of the same validator should be the same
                    // (this doesn't)
                    // The act of validation changes something in the
                    // model......
                    assertTrue(
                            resultsComparisonMessage(veraCorpus.getDetails()
                                    .getName(), itemName, profile
                                    .getPDFAFlavour().toString(), firstResult,
                                    secondResult),
                            firstResult.equals(secondResult));
                }
            }
        }
    }

    private static boolean checkValidationException(final String itemName,
            final ValidationException excep) {
        if (!(excep.getCause() instanceof NegativeArraySizeException)) {
            excep.printStackTrace();
            fail("Exception" + excep.getMessage() + ", while validating"
                    + itemName);
        }
        return true;
    }

    private String testContextMessage(final String corpus,
            final String itemName, final String profile) {
        return "corpus=" + corpus + "\nitemName=" + itemName + "\nprofile="
                + profile + "\n";
    }

    private String resultsMessage(final String corpus, final String itemName,
            final String profile, final Set<ValidationResult> results)
            throws JAXBException {
        StringWriter writer = new StringWriter();
        writer.write(testContextMessage(corpus, itemName, profile));
        writer.write("\nSet<ValidationResults>.size()=" + results.size()
                + "\nResults:\n");

        if (results.size() > 0) {
            for (ValidationResult result : results) {
            	XmlSerialiser.toXml(result, writer, true, true);
            }
        }
        return writer.toString();
    }

    private String resultsComparisonMessage(final String corpus,
            final String itemName, final String profile,
            final ValidationResult firstResult,
            final ValidationResult secondResult) throws JAXBException {
        StringWriter writer = new StringWriter();
        writer.write(testContextMessage(corpus, itemName, profile));
        writer.write("\nFirstResult:\n");
        XmlSerialiser.toXml(firstResult, writer, true, true);
        writer.write("\nSecondResult:\n");
        XmlSerialiser.toXml(secondResult, writer, true, true);
        return writer.toString();
    }
    
    private int getMaxFailureOrdinal(final ValidationResult result) {
        int maxOrdinal = 0;
        for (TestAssertion assertion : result.getTestAssertions()) {
            if ((assertion.getStatus() == Status.FAILED) && (assertion.getOrdinal() > maxOrdinal))
                maxOrdinal = assertion.getOrdinal();
        }
        return maxOrdinal;
    }
}

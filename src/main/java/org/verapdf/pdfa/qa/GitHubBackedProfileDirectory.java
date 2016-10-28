/**
 * 
 */
package org.verapdf.pdfa.qa;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.results.ValidationResults;
import org.verapdf.pdfa.validation.ProfileDirectory;
import org.verapdf.pdfa.validation.Profiles;
import org.verapdf.pdfa.validation.ValidationProfile;

/**
 * @author <a href="mailto:carl@openpreservation.org">Carl Wilson</a>
 */
public class GitHubBackedProfileDirectory implements ProfileDirectory {
	private static final Logger LOG = Logger.getLogger(GitHubBackedProfileDirectory.class.getCanonicalName());
	/**
	 * This would work for Master branch profiles MASTER("master");
	 */
	private static final String GITHUB_ROOT = "https://raw.githubusercontent.com/veraPDF/veraPDF-validation-profiles/";
	private static final String PROFILE_PATH_PART = "/PDF_A/";
	private static final String PROFILE_PREFIX = "PDFA-";
	private static final String XML_SUFFIX = ".xml";
	private final String branchName;
	private final ProfileDirectory profiles;

	GitHubBackedProfileDirectory(final String branchName) {
		this.branchName = branchName;
		this.profiles = Profiles.directoryFromProfiles(fromGitHubBranch(this.branchName));
	}

	/**
	 * @return the GitHub branch name that the instance is populated from
	 */
	public String getBranchName() {
		return this.branchName;
	}

	/**
	 * { @inheritDoc }
	 */
	@Override
	public Set<String> getValidationProfileIds() {
		return this.profiles.getValidationProfileIds();
	}

	/**
	 * { @inheritDoc }
	 */
	@Override
	public Set<PDFAFlavour> getPDFAFlavours() {
		return this.profiles.getPDFAFlavours();
	}

	/**
	 * { @inheritDoc }
	 */
	@Override
	public ValidationProfile getValidationProfileById(final String profileID) {
		return this.profiles.getValidationProfileById(profileID);
	}

	/**
	 * { @inheritDoc }
	 */
	@Override
	public ValidationProfile getValidationProfileByFlavour(final PDFAFlavour flavour) {
		return this.profiles.getValidationProfileByFlavour(flavour);
	}

	/**
	 * { @inheritDoc }
	 */
	@Override
	public Set<ValidationProfile> getValidationProfiles() {
		return this.profiles.getValidationProfiles();
	}

	public static ProfileDirectory fromBranch(final String branchName) {
		return new GitHubBackedProfileDirectory(branchName);
	}
	private static Set<ValidationProfile> fromGitHubBranch(final String branchName) {
		String pathPrefix = GITHUB_ROOT + branchName + PROFILE_PATH_PART + PROFILE_PREFIX;
		Set<ValidationProfile> profileSet = new HashSet<>();
		for (PDFAFlavour flavour : PDFAFlavour.values()) {
			if (flavour == PDFAFlavour.NO_FLAVOUR || flavour == PDFAFlavour.AUTO) {
				continue;
			}
			String profileURLString = pathPrefix + flavour.getId().toUpperCase() + XML_SUFFIX;
			try {
				URL profileURL = new URL(profileURLString);
				ValidationProfile profile = Profiles.profileFromXml(profileURL.openStream());
				profileSet.add(profile);
			} catch (IOException | JAXBException e) {
				LOG.log(Level.SEVERE, "Exception when trying to load validation profile from:" + profileURLString,
						e);
			}
		}
		return profileSet;
	}
}

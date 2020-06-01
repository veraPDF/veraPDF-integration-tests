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
package org.verapdf.pdfa.qa;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.validation.profiles.ProfileDirectory;
import org.verapdf.pdfa.validation.profiles.Profiles;
import org.verapdf.pdfa.validation.profiles.ValidationProfile;

/**
 * @author <a href="mailto:carl@openpreservation.org">Carl Wilson</a>
 */
public class GitHubBackedProfileDirectory implements ProfileDirectory {
	private static final Logger LOG = Logger.getLogger(GitHubBackedProfileDirectory.class.getCanonicalName());
	/**
	 * This would work for Master branch profiles MASTER("master");
	 */
	private static final String GITHUB_ROOT = "https://raw.githubusercontent.com/veraPDF/veraPDF-validation-profiles/";

	private static final String PDFA_PROFILE_PATH_PART = "/PDF_A/";
	private static final String PDFA_PROFILE_PREFIX = "PDFA-";
	private static final String PDFUA_PROFILE_PATH_PART = "/PDF_UA/";
	private static final String PDFUA_PROFILE_PREFIX = "PDFUA-";
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
		String PDFApathPrefix = GITHUB_ROOT + branchName + PDFA_PROFILE_PATH_PART + PDFA_PROFILE_PREFIX;
		String PDFUApathPrefix = GITHUB_ROOT + branchName + PDFUA_PROFILE_PATH_PART + PDFUA_PROFILE_PREFIX;
		Set<ValidationProfile> profileSet = new HashSet<>();
		for (PDFAFlavour flavour : PDFAFlavour.values()) {
			if (flavour == PDFAFlavour.NO_FLAVOUR || flavour == PDFAFlavour.PDFA_4) {
				continue;
			}
			String profileURLString = (flavour != PDFAFlavour.PDFUA_1 ? PDFApathPrefix : PDFUApathPrefix)
					+ flavour.getPart().getPartNumber() + flavour.getLevel().getCode().toUpperCase() + XML_SUFFIX;;
			try {
				URL profileURL = new URL(profileURLString);
				ValidationProfile profile = Profiles.profileFromXml(profileURL.openStream());
				profileSet.add(profile);
			} catch (FileNotFoundException fnf) {
				// Couldn't load the profile from GitHun log and continue
				LOG.log(Level.WARNING, String.format("Couldn't find GitHub Validation Profile for flavour %s", flavour));
				LOG.log(Level.WARNING, String.format("Effectively a 404 for %s", profileURLString));
				LOG.log(Level.WARNING, "FileNotFoundException caught.", fnf);
				
			} catch	(IOException | JAXBException e) {
				LOG.log(Level.SEVERE, "Exception when trying to load validation profile from:" + profileURLString,
						e);
			}
		}
		return profileSet;
	}
}

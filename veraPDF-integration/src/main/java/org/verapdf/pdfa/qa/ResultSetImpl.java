/**
 * This file is part of veraPDF Quality Assurance, a module of the veraPDF
 * project. Copyright (c) 2015, veraPDF Consortium <info@verapdf.org> All rights
 * reserved. veraPDF Quality Assurance is free software: you can redistribute it
 * and/or modify it under the terms of either: The GNU General public license
 * GPLv3+. You should have received a copy of the GNU General Public License
 * along with veraPDF Quality Assurance as the LICENSE.GPL file in the root of
 * the source tree. If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html. The Mozilla Public License
 * MPLv2+. You should have received a copy of the Mozilla Public License along
 * with veraPDF Quality Assurance as the LICENSE.MPL file in the root of the
 * source tree. If a copy of the MPL was not distributed with this file, you can
 * obtain one at http://mozilla.org/MPL/2.0/.
 */
/**
 * 
 */
package org.verapdf.pdfa.qa;

import org.verapdf.component.AuditDuration;
import org.verapdf.component.Components;
import org.verapdf.pdfa.Foundries;
import org.verapdf.pdfa.PDFAParser;
import org.verapdf.pdfa.PDFAValidator;
import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.flavours.PDFFlavours;
import org.verapdf.pdfa.results.ValidationResult;
import org.verapdf.pdfa.validation.profiles.Profiles;
import org.verapdf.pdfa.validation.profiles.ValidationProfile;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:carl@openpreservation.org">Carl Wilson</a>
 */
public class ResultSetImpl implements ResultSet {
	private static final int MEGABYTE = (1024 * 1024);
	private static final Logger LOG = Logger.getLogger(ResultSetImpl.class.getCanonicalName());
	@XmlElement(name = "resultDetails")
	private final ResultSetDetails details = ResultSetDetailsImpl
			.getNewInstance(Foundries.defaultInstance().getDetails());
	@XmlElement(name = "corpusDetails")
	private final CorpusDetails corpusDetails;
    @XmlElement(name = "corpusId")
    private final String corpusId;
	@XmlElement(name = "profile")
	private final ValidationProfile profile;
	@XmlElement(name = "summary")
	private final ResultSetSummary summary;
	@XmlElementWrapper
	@XmlElement(name = "result")
	private final SortedSet<Result> results;
	@XmlElementWrapper
	@XmlElement(name = "exception")
	private final Set<Incomplete> exceptions;

	private ResultSetImpl(final CorpusDetails corpusDetails, final String corpusId, final ValidationProfile profile, final Set<Result> results,
			final Set<Incomplete> exceptions, final AuditDuration duration, final long memoryUsed) {
		this.corpusDetails = corpusDetails;
        this.corpusId = corpusId;
		this.profile = profile;
		this.results = new TreeSet<>(new ResultComparator());
		this.results.addAll(results);
		this.summary = ResultSetSummaryImpl.fromResults(results, exceptions, duration, memoryUsed);
		this.exceptions = new HashSet<>(exceptions);
	}

	/**
	 * { @inheritDoc }
	 */
	@Override
	public ResultSetDetails getDetails() {
		return this.details;
	}

	/**
	 * { @inheritDoc }
	 */
	@Override
	public CorpusDetails getCorpusDetails() {
		return this.corpusDetails;
	}

	/**
	 * { @inheritDoc }
	 */
	@Override
	public String getCorpusId() {
		return this.corpusId;
	}

	/**
	 * { @inheritDoc }
	 */
	@Override
	public ValidationProfile getValidationProfile() {
		return this.profile;
	}

	/**
	 * { @inheritDoc }
	 */
	@Override
	public ResultSetSummary getSummary() {
		return this.summary;
	}

	/**
	 * { @inheritDoc }
	 */
	@Override
	public Set<Result> getResults() {
		return this.results;
	}

	/**
	 * { @inheritDoc }
	 */
	@Override
	public Set<Incomplete> getExceptions() {
		return this.exceptions;
	}

	/**
	 * { @inheritDoc }
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.corpusDetails == null) ? 0 : this.corpusDetails.hashCode());
		result = prime * result + ((this.details == null) ? 0 : this.details.hashCode());
		result = prime * result + ((this.profile == null) ? 0 : this.profile.hashCode());
		result = prime * result + ((this.results == null) ? 0 : this.results.hashCode());
		result = prime * result + ((this.exceptions == null) ? 0 : this.results.hashCode());
		return result;
	}

	/**
	 * { @inheritDoc }
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ResultSet))
			return false;
		ResultSet other = (ResultSet) obj;
		if (this.corpusDetails == null) {
			if (other.getCorpusDetails() != null)
				return false;
		} else if (!this.corpusDetails.equals(other.getCorpusDetails()))
			return false;
		if (this.details == null) {
			if (other.getDetails() != null)
				return false;
		} else if (!this.details.equals(other.getDetails()))
			return false;
		if (this.profile == null) {
			if (other.getValidationProfile() != null)
				return false;
		} else if (!this.profile.equals(other.getValidationProfile()))
			return false;
		if (this.results == null) {
			if (other.getResults() != null)
				return false;
		} else if (!this.results.equals(other.getResults()))
			return false;
		if (this.exceptions == null) {
			if (other.getExceptions() != null)
				return false;
		} else if (!this.exceptions.equals(other.getExceptions()))
			return false;
		return true;
	}

	/**
	 * { @inheritDoc }
	 */
	@Override
	public String toString() {
		return "ResultSet [details=" + this.details + ", corpusDetails=" + this.corpusDetails + ", profile="
				+ this.profile + ", results=" + this.results + ", exceptions=" + this.exceptions + "]";
	}

	/**
	 * @param corpus
	 * @param validator
	 * @return
	 */
	public static ResultSet validateCorpus(final TestCorpus corpus, final PDFAValidator validator, final PDFAFlavour flavour) {
		Set<Result> results = new HashSet<>();
		Set<Incomplete> exceptions = new HashSet<>();
		Components.Timer batchTimer = Components.Timer.start();
		long maxMemUse = 0;
		for (String itemName : corpus.getItemNames()) {
			System.out.println(itemName);
			CorpusItemId id = null;
			Components.Timer jobTimer = Components.Timer.start();
			try (PDFAParser loader = Foundries.defaultInstance().createParser(corpus.getItemStream(itemName), flavour);
				 PDFAValidator newValidator = flavour != PDFAFlavour.NO_FLAVOUR ? null : Foundries.defaultInstance().createValidator(loader.getFlavour(), false)) {
				PDFAValidator currentValidator = flavour != PDFAFlavour.NO_FLAVOUR ? validator : newValidator;
				try {
					id = CorpusItemIdImpl.fromFileName(currentValidator.getProfile().getPDFAFlavour().getPart(), itemName, "");
				} catch (IllegalArgumentException excep) {
					LOG.log(Level.FINE, "Problem generating ID for corpus item:" + itemName, excep);
				}
				ValidationResult result = currentValidator.validate(loader);
				long memUsed = (ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() / MEGABYTE);
					maxMemUse = Math.max(memUsed, maxMemUse);
				results.add(new Result(id, result, jobTimer.stop(), memUsed));
			} catch (Throwable e) {
				LOG.log(Level.SEVERE, String.format("Caught throwable testing %s from corpus %s", itemName,
						corpus.getDetails().getName()));
				LOG.log(Level.SEVERE, e.getClass().getName());
				LOG.log(Level.SEVERE, e.getMessage());
				exceptions.add(new Incomplete(id, e));
			}
		}
		return new ResultSetImpl(corpus.getDetails(), corpus.getType().getId(), flavour != PDFAFlavour.NO_FLAVOUR ? 
				validator.getProfile() : Profiles.defaultProfile(), results, exceptions, batchTimer.stop(),
				maxMemUse);
	}

	static class Adapter extends XmlAdapter<ResultSetImpl, ResultSet> {
		@Override
		public ResultSet unmarshal(ResultSetImpl results) {
			return results;
		}

		@Override
		public ResultSetImpl marshal(ResultSet results) {
			return (ResultSetImpl) results;
		}
	}
}

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

import org.apache.commons.codec.digest.DigestUtils;
import org.verapdf.pdfa.flavours.PDFAFlavour;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * @author <a href="mailto:carl@openpreservation.org">Carl Wilson</a>
 */
public class ZipBackedTestCorpus extends AbstractTestCorpus<ZipEntry> {
	private final static String PDF_SUFFIX = ".pdf";
	private final ZipFile zipSource;

	private ZipBackedTestCorpus(final CorpusDetails details, final Corpus type, final File zipSource,
			final PDFAFlavour flavour) throws ZipException, IOException {
		super(details, type, itemsMapFromZipSource(zipSource, flavour, type));
		this.zipSource = new ZipFile(zipSource);
	}

	/**
	 * { @inheritDoc }
	 *
	 * @throws IOException
	 */
	@Override
	protected InputStream getStreamFromReference(ZipEntry reference) throws IOException {
		return this.zipSource.getInputStream(reference);
	}

	/**
	 * @param name
	 *            a String name for the TestCorpus instance
	 * @param description
	 *            a textual description of the TestCorpus instance
	 * @param zipFile
	 *            a {@link File} instance that's a zip file for the corpus
	 * @return a TestCorpus instance initialised from the passed params and zip
	 *         file
	 * @throws IOException
	 *             if there's an exception parsing the zip file
	 * @throws ZipException
	 *             if there's an exception parsing the zip file
	 */
	public static TestCorpus fromZipSource(final String name, Corpus type, final String description,
			final PDFAFlavour flavour) throws ZipException, IOException {
		if (name == null)
			throw new NullPointerException("Parameter name can not be null");
		if (name.isEmpty())
			throw new NullPointerException("Parameter name can not be empty");
		if (description == null)
			throw new NullPointerException("Parameter description can not be null");
		String hexSha1 = "";
		try (InputStream is = new FileInputStream(type.getZipFile())) {
			hexSha1 = DigestUtils.sha1Hex(is);
		}
		System.out.println("Loading corpus:" + type);
		return new ZipBackedTestCorpus(CorpusDetailsImpl.fromValues(name, description, hexSha1), type, type.getZipFile(),
				flavour);
	}

	private static final Map<String, ZipEntry> itemsMapFromZipSource(final File zipFile, final PDFAFlavour flavour, Corpus type)
			throws ZipException, IOException {
		Map<String, ZipEntry> itemMap = new HashMap<>();

		try (ZipFile zipSource = new ZipFile(zipFile)) {
			Enumeration<? extends ZipEntry> entries = zipSource.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (entry.isDirectory() || !entry.getName().endsWith(PDF_SUFFIX))
					continue;
				String entryName = entry.getName();
				if (type == Corpus.TWG && entryName.contains("TWG")) {
					itemMap.put(entryName, entry);
				} else if (flavour == null || type == Corpus.BFO) {
					itemMap.put(entryName, entry);
				} else if (checkFlavour(entryName, flavour)) {
					itemMap.put(entryName, entry);
				}
			}
		}
		return itemMap;
	}

	public static boolean checkFlavour(final String item, final PDFAFlavour flavour) {
		if (flavour == PDFAFlavour.PDFUA_1) {
			return item.contains("PDF_UA-1");
		}
		if (flavour == PDFAFlavour.PDFUA_2) {
			return item.contains("PDF_UA-2");
		}
		if (flavour == PDFAFlavour.PDFA_4) {
			return item.contains("PDF_A-4") && !matchFlavour(item, PDFAFlavour.PDFA_4_E) && !matchFlavour(item, PDFAFlavour.PDFA_4_F);
		}
		return matchFlavour(item, flavour);
	}

	public static boolean matchFlavour(final String item, final PDFAFlavour flavour) {
		return flavour != PDFAFlavour.NO_FLAVOUR && item.matches(String.format(".*PDF_?A-%s.*", flavour.getId()));
	}
}

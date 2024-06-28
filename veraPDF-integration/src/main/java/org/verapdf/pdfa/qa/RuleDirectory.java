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

import org.verapdf.core.Directory;
import org.verapdf.core.MapBackedRegistry;
import org.verapdf.core.Registry;
import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.validation.profiles.*;

import jakarta.xml.bind.JAXBException;
import java.io.*;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:carl@openpreservation.org">Carl Wilson</a>
 */
public final class RuleDirectory implements Directory<RuleId, Rule> {

    private final Registry<RuleId, Rule> rules = new MapBackedRegistry<>(
            Collections.emptyMap());
    private final Set<Variable> variables = new HashSet<>();
    private final PDFAFlavour flavour;

    private RuleDirectory(final File root) throws FileNotFoundException,
            IOException, JAXBException {
        this.flavour = PDFAFlavour.fromString(root.getName());
        Set<Rule> ruleSet = rulesFromDir(root, this.flavour);
        for (Rule rule : ruleSet) {
            this.rules.putdateItem(rule.getRuleId(), rule);
        }
    }

    public Set<Variable> getVariables() {
        return Collections.unmodifiableSet(this.variables);
    }

    /**
     * @see Directory#getItem(Object)
     */
    @Override
    public Rule getItem(RuleId key) {
        return this.rules.getItem(key);
    }

    /**
     * @see Directory#getItems()
     */
    @Override
    public Collection<Rule> getItems() {
        return this.rules.getItems();
    }

    /**
     * @see Directory#getKeys()
     */
    @Override
    public Set<RuleId> getKeys() {
        return this.rules.getKeys();
    }

    /**
     * @see Directory#size()
     */
    @Override
    public int size() {
        return this.rules.size();
    }

    /**
     * @see Directory#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return this.rules.isEmpty();
    }

    /**
     * @return the {@link PDFAFlavour} associated with the {@code RuleId}.
     */
    public PDFAFlavour getFlavour() {
        return this.flavour;
    }

    /**
     * Load up a {@link Rule} {@code Set} from a root directory
     *
     * @param root the {@code File} root directory for the Corpus
     * @return a new {@link RuleDirectory} instance initialised from
     * {@code root}.
     * @throws NullPointerException     if {@code root} is null
     * @throws IllegalArgumentException if {@code root} is not an existing directory
     * @throws JAXBException            if one of the Rules could not be parsed
     * @throws IOException              if there's a problem reading the directory contents
     */
    public static RuleDirectory loadFromDir(final File root)
            throws IOException, JAXBException {
        if (root == null)
            throw new NullPointerException("Parameter root should not be null.");
        if (!root.isDirectory())
            throw new IllegalArgumentException(
                    "Parameter root MUST be an existing directory.");
        return new RuleDirectory(root);
    }

    private Set<Rule> rulesFromDir(final File dir,
                                   final PDFAFlavour pdfaFlavour) throws FileNotFoundException,
            IOException, JAXBException {
        Set<Rule> rulesLocal = new HashSet<>();
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isHidden())
                continue;
            if (file.isDirectory())
            	rulesLocal.addAll(rulesFromDir(file, pdfaFlavour));
            if (file.isFile() && file.canRead()) {
                try (InputStream fis = new FileInputStream(file)) {
                    Set<Rule> rule = getRuleFromProfile(fis);
                    rulesLocal.addAll(rule);
                }
            }
        }
        return rulesLocal;
    }

    private Set<Rule> getRuleFromProfile(final InputStream toParse) throws JAXBException {
        ValidationProfile profile = Profiles.profileFromXml(
                toParse);
        checkAndAddAllVariables(this.variables, profile.getVariables());
        return profile.getRules();
    }

    static void checkAndAddAllVariables(Set<Variable> toAdd, Set<Variable> fromAdd) {
        for (Variable var : fromAdd) {
            for (Variable toVar : toAdd) {
                if (toVar.getName().equals(var.getName()) && !toVar.equals(var)) {
                    throw new IllegalArgumentException("Found different variables with the same name.");
                }
            }
            toAdd.add(var);
        }
    }
}

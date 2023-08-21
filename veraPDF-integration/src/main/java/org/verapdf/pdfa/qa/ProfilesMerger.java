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
package org.verapdf.pdfa.qa;

import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.validation.profiles.*;
import org.verapdf.pdfa.validation.profiles.Profiles.RuleComparator;

import jakarta.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * @author Maksim Bezrukov
 */
public class ProfilesMerger {

    public static void main(String[] args) {
        if (args.length < 4) {
            throw new IllegalArgumentException("There must be at least four arguments");
        }

        File[] directories = new File[args.length - 3];
        for (int i = 3; i < args.length; ++i) {
            File dir = new File(args[i]);
            if (!dir.isDirectory()) {
                throw new IllegalArgumentException("All entered arguments after the third one " +
                        "should point to the folder with the profiles to merge");
            }
            directories[i - 3] = dir;
        }

        try {
            mergeAtomicProfiles(System.out, directories, args[0], args[1], args[2]);
        } catch (IOException | JAXBException e) {
            e.printStackTrace();
        }
    }

    public static void mergeAtomicProfiles(OutputStream out,
                                           final File[] root,
                                           final String name,
                                           final String description,
                                           final String creator) throws IOException, JAXBException {
        SortedSet<Rule> rules = new TreeSet<>(new RuleComparator());
        Set<Variable> variables = new HashSet<>();
        PDFAFlavour flavour = null;

        for (File dir : root) {
            RuleDirectory ruleDir = RuleDirectory.loadFromDir(dir);
            if (flavour == null) {
                flavour = ruleDir.getFlavour();
            }
            rules.addAll(updateSpecification(ruleDir.getItems(), flavour));
            RuleDirectory.checkAndAddAllVariables(variables, ruleDir.getVariables());
        }

        ProfileDetails det = Profiles.profileDetailsFromValues(name, description, creator, new Date());
        ValidationProfile mergedProfile = Profiles.profileFromSortedValues(flavour, det, "", rules, variables);
        Profiles.profileToXml(mergedProfile, out, true, false);
    }

    private static Set<Rule> updateSpecification(final Collection<Rule> rules, final PDFAFlavour flavour) {
        Set<Rule> res = new HashSet<>(rules.size());
        for (Rule r : rules) {
            RuleId id = Profiles.ruleIdFromValues(flavour.getPart(), r.getRuleId().getClause(), r.getRuleId().getTestNumber());
            res.add(Profiles.ruleFromValues(id, r.getObject(), r.getDeferred(), r.getTags(), r.getDescription(), r.getTest(),
                    r.getError(), r.getReferences()));
        }
        return res;
    }
}

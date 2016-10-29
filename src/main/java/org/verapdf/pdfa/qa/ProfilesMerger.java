package org.verapdf.pdfa.qa;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.bind.JAXBException;

import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.validation.profiles.ProfileDetails;
import org.verapdf.pdfa.validation.profiles.Profiles;
import org.verapdf.pdfa.validation.profiles.Profiles.RuleComparator;
import org.verapdf.pdfa.validation.profiles.Rule;
import org.verapdf.pdfa.validation.profiles.RuleId;
import org.verapdf.pdfa.validation.profiles.ValidationProfile;
import org.verapdf.pdfa.validation.profiles.Variable;

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
        Profiles.profileToXml(mergedProfile, out, Boolean.TRUE);
    }

    private static Set<Rule> updateSpecification(final Collection<Rule> rules, final PDFAFlavour flavour) {
        Set<Rule> res = new HashSet<>(rules.size());
        for (Rule r : rules) {
            RuleId id = Profiles.ruleIdFromValues(flavour.getPart(), r.getRuleId().getClause(), r.getRuleId().getTestNumber());
            res.add(Profiles.ruleFromValues(id, r.getObject(), r.getDescription(), r.getTest(), r.getError(), r.getReferences()));
        }
        return res;
    }
}

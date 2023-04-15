import fr.boreal.forward_chaining.api.ForwardChainingAlgorithm;
import fr.boreal.forward_chaining.chase.ChaseBuilder;
import fr.boreal.io.api.ParseException;
import fr.boreal.io.dlgp.impl.builtin.DlgpParser;
import fr.boreal.model.kb.api.FactBase;
import fr.boreal.model.kb.api.RuleBase;
import fr.boreal.model.kb.impl.RuleBaseImpl;
import fr.boreal.model.logicalElements.api.Atom;
import fr.boreal.model.logicalElements.api.Substitution;
import fr.boreal.model.logicalElements.factory.api.PredicateFactory;
import fr.boreal.model.logicalElements.factory.api.TermFactory;
import fr.boreal.model.logicalElements.factory.impl.SameObjectPredicateFactory;
import fr.boreal.model.logicalElements.factory.impl.SameObjectTermFactory;
import fr.boreal.model.query.api.FOQuery;
import fr.boreal.model.rule.api.FORule;
import fr.boreal.queryEvaluation.generic.GenericFOQueryEvaluator;
import fr.boreal.storage.builder.StorageBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class TestFermeture {
    public static void main(String[] args) throws Exception {

        ///////////////
        // Factories //
        ///////////////

        TermFactory termfactory = SameObjectTermFactory.instance();
        PredicateFactory predicatefactory = SameObjectPredicateFactory.instance();

        //////////////////
        // DLGP parsing //
        //////////////////

        System.out.println("Importing dlgp file ...");
        Collection<FORule> rules = new ArrayList<FORule>();


        for(String filepath : new String[] {
                "ruleBase.dlgp"
                // available at https://notes.inria.fr/Rc2uiwUfQoSxb06-Ex4Jpw
        }) {

            File file = new File(filepath);
            DlgpParser dlgp_parseur = new DlgpParser(file, termfactory, predicatefactory);
            while (dlgp_parseur.hasNext()) {
                try {
                    Object result = dlgp_parseur.next();
                     if (result instanceof FORule) {
                        rules.add((FORule)result);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            dlgp_parseur.close();
        }



        RuleBase rb = new RuleBaseImpl(rules);


        System.out.println("RuleBase : ");
        System.out.println(rb);
        RuleBase rb1 = ForgettingIntegraal.rbSaturation(rb);
        System.out.println("Saturated RuleBase :");
        System.out.println(rb1);

    }
}

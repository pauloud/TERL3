import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

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

public class ExempleFlorent {

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
        Collection<Atom> atoms = new ArrayList<Atom>();
        Collection<FORule> rules = new ArrayList<FORule>();
        Collection<FOQuery> queries = new ArrayList<FOQuery>();

        for(String filepath : new String[] {
                "example.dlgp"
                // available at https://notes.inria.fr/Rc2uiwUfQoSxb06-Ex4Jpw
        }) {

            File file = new File(filepath);
            DlgpParser dlgp_parseur = new DlgpParser(file, termfactory, predicatefactory);
            while (dlgp_parseur.hasNext()) {
                try {
                    Object result = dlgp_parseur.next();
                    if (result instanceof Atom) {
                        atoms.add((Atom)result);
                    } else if (result instanceof FORule) {
                        rules.add((FORule)result);
                    } else if (result instanceof FOQuery) {
                        queries.add((FOQuery)result);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            dlgp_parseur.close();
        }

        FactBase fb = StorageBuilder.getSimpleInMemoryGraphStore();
        fb.addAll(atoms);

        RuleBase rb = new RuleBaseImpl(rules);

        System.out.println("FactBase : ");
        System.out.println(fb);
        System.out.println("RuleBase : ");
        System.out.println(rb);
        System.out.println("Queries : ");
        System.out.println(queries);
        ForwardChainingAlgorithm chase = ChaseBuilder.defaultChase(fb, rb, termfactory);
        chase.execute();
        System.out.println("---");
        System.out.println("Evaluating queries ...");
        for(FOQuery q : queries) {
            System.out.println("Query " + q);
            Iterator<Substitution> res = GenericFOQueryEvaluator.defaultInstance().evaluate(q, fb);
            if(res.hasNext()) {
                res.forEachRemaining(System.out::println);
            } else {
                System.out.println("No answer");
            }
        }

    }

}

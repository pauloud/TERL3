import fr.boreal.io.api.ParseException;
import fr.boreal.io.dlgp.impl.builtin.DlgpParser;
import fr.boreal.model.kb.api.RuleBase;
import fr.boreal.model.kb.impl.RuleBaseImpl;
import fr.boreal.model.logicalElements.factory.api.PredicateFactory;
import fr.boreal.model.logicalElements.factory.api.TermFactory;
import fr.boreal.model.logicalElements.factory.impl.SameObjectPredicateFactory;
import fr.boreal.model.logicalElements.factory.impl.SameObjectTermFactory;
import fr.boreal.model.rule.api.FORule;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

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
                args.length > 0 ? args[0] : "ruleBase.dlgp"

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
        RuleBase rb1 = Forgetting.rbUnfolding(rb);
        System.out.println("Saturated RuleBase :");
        System.out.println(rb1);
        System.out.println("And after compiling :");
        System.out.println(Forgetting.compileRuleBase(rb));

    }
}

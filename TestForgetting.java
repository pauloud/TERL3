import fr.boreal.io.api.ParseException;
import fr.boreal.io.dlgp.impl.builtin.DlgpParser;
import fr.boreal.io.dlgp.impl.builtin.DlgpWriter;
import fr.boreal.model.kb.api.RuleBase;
import fr.boreal.model.kb.impl.RuleBaseImpl;
import fr.boreal.model.logicalElements.factory.api.PredicateFactory;
import fr.boreal.model.logicalElements.factory.api.TermFactory;
import fr.boreal.model.logicalElements.factory.impl.SameObjectPredicateFactory;
import fr.boreal.model.logicalElements.factory.impl.SameObjectTermFactory;
import fr.boreal.model.rule.api.FORule;

import java.io.File;
import java.util.*;


public class TestForgetting {
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
        Scanner predicateScanner = new Scanner (new File(args.length >= 1 ? args[0] : "toForget"));
        ArrayList<String> toForget = new ArrayList<>();
        while (predicateScanner.hasNextLine()){
            toForget.add(predicateScanner.nextLine().trim());
        }
        RuleBase rb1 = Forgetting.forget(rb, new HashSet<>(toForget));
        DlgpWriter writer = new DlgpWriter();
        System.out.println("After Forgetting of " + toForget + " :");
        writer.write(rb1);
        //quand writer a commencé à utiliser la sortie standard on ne peut plus afficher avec System.out.println
        writer.write("And after compiling :\n");
        writer.write(Forgetting.compileRuleBase(rb1));
        writer.close();
    }
}

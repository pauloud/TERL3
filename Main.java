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


public class Main {

    public static void reply (String rulesPath, String toForgetPath, String outputPath) throws Exception{
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
                rulesPath != null ? rulesPath : "ruleBase.dlgp"

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


        DlgpWriter writer = outputPath != null ? new DlgpWriter(outputPath) : new DlgpWriter();
       writer.write("RuleBase : \n");
        writer.write(rb);
        Scanner predicateScanner = new Scanner (new File(toForgetPath != null ? toForgetPath : "toForget"));
        ArrayList<String> toForget = new ArrayList<>();
        while (predicateScanner.hasNextLine()){
            toForget.add(predicateScanner.nextLine().trim());
        }
        RuleBase rb1 = Forgetting.forget(rb, new HashSet<>(toForget));
        writer.write("After Forgetting of " + toForget + " :\n");
        writer.write(rb1);
        //quand writer a commencé à utiliser la sortie standard on ne peut plus afficher avec System.out.println
        writer.write("And after compiling :\n");
        writer.write(Forgetting.compileRuleBase(rb1));
        writer.write ("Original rulebase after optimized forgetting\n");
        writer.write(Forgetting.forgetAndCompile(rb,new HashSet <> (toForget)));
        writer.close();
    }
    public static void main(String[] args) throws Exception {
        reply(ParseArguments.parseRulesPath(args),ParseArguments.parseToForgetPath(args),ParseArguments.parseOutputPath(args));


    }
}

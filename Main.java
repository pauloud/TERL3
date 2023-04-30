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


        System.out.println("RuleBase : ");
        System.out.println(rb);
        Scanner predicateScanner = new Scanner (new File(toForgetPath != null ? toForgetPath : "toForget"));
        ArrayList<String> toForget = new ArrayList<>();
        while (predicateScanner.hasNextLine()){
            toForget.add(predicateScanner.nextLine().trim());
        }
        RuleBase rb1 = Forgetting.forget(rb, new HashSet<>(toForget));
        DlgpWriter writer = outputPath != null ? new DlgpWriter(outputPath) : new DlgpWriter();
        System.out.println("After Forgetting of " + toForget + " :");
        writer.write(rb1);
        //quand writer a commencé à utiliser la sortie standard on ne peut plus afficher avec System.out.println
        writer.write("And after compiling :\n");
        writer.write(Forgetting.compileRuleBase(rb1));
        writer.close();
    }
    public static void main(String[] args) throws Exception {
        String rulesPath = args.length > 0 ? args[0] : null;
        String toForgetPath = args.length == 2 ? args[1] :null;
        String outputPath = args.length == 3 ? args[2] : null;
        for (int i = 1 ; i < args.length; i++){
            if (args[i] == "-f" && args.length > i+1)
                toForgetPath = args[i+1];
            if (args[i] == "-o" && args.length > i+1)
                outputPath= args[i+1];
            
        }
        reply(rulesPath,toForgetPath,outputPath);


    }
}

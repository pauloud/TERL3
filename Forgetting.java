import fr.boreal.model.formula.api.FOFormula;
import fr.boreal.model.formula.api.FOFormulaConjunction;
import fr.boreal.model.formula.impl.AtomicFOImpl;
import fr.boreal.model.kb.api.FactBase;
import fr.boreal.model.kb.api.RuleBase;
import fr.boreal.model.kb.impl.RuleBaseImpl;
import fr.boreal.model.logicalElements.api.Atom;
import fr.boreal.model.logicalElements.api.Substitution;
import fr.boreal.model.logicalElements.impl.SubstitutionImpl;
import fr.boreal.model.logicalElements.impl.identityObjects.IdentityConstantImpl;
import fr.boreal.model.query.api.AtomicFOQuery;
import fr.boreal.model.query.api.FOQuery;
import fr.boreal.model.query.api.FOQueryConjunction;
import fr.boreal.model.query.impl.AtomicFOQueryImpl;
import fr.boreal.model.query.impl.FOQueryConjunctionImpl;
import fr.boreal.model.queryEvaluation.api.FOQueryEvaluator;
import fr.boreal.model.rule.impl.FORuleImpl;
import fr.boreal.queryEvaluation.generic.GenericFOQueryEvaluator;
import fr.boreal.storage.inmemory.DefaultInMemoryAtomSet;
import fr.boreal.model.rule.api.FORule;
import fr.boreal.backward_chaining.pure.PureRewriterOptimized;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;


import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

 /*class GreenRed {
     RuleBase green;
     RuleBase red;

     GreenRed(Stream <FORule> rules, Set<String> toForget){
         Collection<FORule> green = new ArrayList<>();
         Collection<FORule> red = new ArrayList<>();
         rules.forEach(r -> {if (Forgetting.unwantedSignatureR(r,toForget)){red.add(r);}
         else {green.add(r);}});
         this.green = new RuleBaseImpl (green);
         this.red = new RuleBaseImpl (red);
     }
    
}*/
public class Forgetting {

    public static RuleBase compileRuleBase(RuleBase rb){
        return compileRuleList(new ArrayList<> (rb.getRules()),new ArrayList<>());
    }


    private static RuleBase compileRuleList (ArrayList<FORule> toCompile, ArrayList<FORule> compiled){
        if (toCompile.isEmpty()){
            return new RuleBaseImpl(compiled); //fin des appels récursifs lorsqu'on a traité toutes les règles
        }
        FORule r = toCompile.get(0);
        toCompile.remove(0);
        RuleBase rb = new RuleBaseImpl(Stream.concat(toCompile.stream(),compiled.stream()).toList());/* base formée de toutes
        les règles sauf r*/

        //code pour tester si r peut être déduite de rb, garde r dans la base compilée uniquement si ça n'est pas le cas

        //transforme le corps de la règle r en base de faits
        Substitution s = new SubstitutionImpl();
        FOFormula<Atom> b = r.getBody().getFormula();//pour pouvoir récupérer toutes les variables du corps de r
        b.getVariables().stream().forEach(v -> s.add(v, new IdentityConstantImpl(v.getLabel())));/*s substitue à chaque variable
         de r une nouvelle constante */
        FactBase fb = new DefaultInMemoryAtomSet(r.getBody().getFormula().flatten().stream().map(a -> s.createImageOf(a)).toList());

        //transforme la tête de la rèble r en requête
        Atom h = s.createImageOf(r.getHead().flatten().stream().findFirst().get());
        AtomicFOQuery q = new AtomicFOQueryImpl(new AtomicFOImpl<>(h),h.getVariables(),null);

        //teste si fb,rb |= q par chainage arrière
        PureRewriterOptimized rewriter = new PureRewriterOptimized();//objet pour réécrire une requête via une base de règles
        Set <FOQuery> queries = rewriter.rewrite(q,rb);
        FOQueryEvaluator evaluator = GenericFOQueryEvaluator.defaultInstance();/*objet pour répondre à une requête
            via uniquement une base de faits */
        if (queries.stream().allMatch(q1 -> ! evaluator.exist(q1,fb)))/*si la base de fait répond "non"
            pour toute requête obtenue par réécriture de q*/
            compiled.add(r);//on ajoute alors r dans les règles que l'on garde

        return compileRuleList(toCompile,compiled);//on teste ensuite les autres règles de la base de règle initiale

    }

    @Contract("_ -> new")
    public static @NotNull RuleBase rbUnfolding(RuleBase rb){
        return new RuleBaseImpl (rbUnfoldingWith(rb,rb.getRules().stream()).toList());

    }
    @Contract("_ -> new")
    private static @NotNull FOQueryConjunction removeSubstitution (@NotNull FOQuery query){
        return new FOQueryConjunctionImpl((FOFormulaConjunction<Atom>) query.getFormula(),query.getAnswerVariables(),null);
    }
    @Contract("_, _ -> new")
    private static @NotNull FOFormula<Atom> applySubstitution (@NotNull FOFormula<Atom> head, @NotNull FOQuery body){
        return new AtomicFOImpl<>(body.getInitialSubstitution().createImageOf(/*
                il faut réécrire la tête au cas où la réécriture du corps ait nécessité des unifications*/
               head.flatten().iterator().next()));//on donne à réécrire l'unique atome de la tête de r

    }
    private static Stream<FORule> rbUnfoldingWith(RuleBase rewriter, @NotNull Stream<FORule> toRewrite){
        PureRewriterOptimized rw = new PureRewriterOptimized();
        return toRewrite.flatMap(r -> rw.rewrite(r.getBody(),rewriter)/* chaque règle, on
            traite chaque réécriture possible du corps*/
                .stream().map(newBody -> (FORule) new FORuleImpl(removeSubstitution(newBody)/*
                pour chaque réécriture du corps obtenu on fabrique une nouvelle règle*/
                        ,applySubstitution(r.getHead(), newBody)

        )));

    }
    public static Stream<FORule> rbUnfoldingComplementWith(RuleBase rewriter, @NotNull Stream<FORule> toRewrite, Set<String> toForget){
        PureRewriterOptimized rw = new PureRewriterOptimized();
        return toRewrite.flatMap(r -> rw.rewrite(r.getBody(),rewriter)/* chaque règle, on
            traite chaque réécriture possible du corps*/
                .stream().filter(newBody -> ! (newBody.equals(r.getBody()) || ! (toForget != null) && unwantedSignatureF(r.getBody().getFormula(),toForget))).map(newBody -> (FORule) new FORuleImpl(removeSubstitution(newBody)/*
                pour chaque réécriture du corps obtenu on fabrique une nouvelle règle*/
                        ,applySubstitution(r.getHead(), newBody)

                )));

    }
 private static boolean unwantedSignatureF (@NotNull FOFormula<Atom> f, Set<String> toForget)
    { return f.getPredicates().stream().anyMatch(p -> toForget.contains(p.getLabel())); }
    static boolean unwantedSignatureR (@NotNull FORule r, Set<String> toForget)
    { return unwantedSignatureF (r.getHead(),toForget) || unwantedSignatureF(r.getBody().getFormula(),toForget) ;}
    public static @NotNull RuleBase forget(@NotNull RuleBase rb, Set<String> toForget){

        Stream<FORule> toRewrite = rb.getRules().stream();
        Stream<FORule> newRules = rbUnfoldingWith(rb,toRewrite);
        return new RuleBaseImpl(Stream.concat(newRules,rb.getRules().stream()).filter(r -> ! unwantedSignatureR(r,toForget)).toList());

    }

    public static RuleBase forgetAndCompile(RuleBase rb,Set<String> toForget){
        ArrayList <FORule> toKeep= new ArrayList<>();
        ArrayList<FORule>  toRewrite = new ArrayList<>();
        ArrayList<FORule> forRewriting = new ArrayList<>();
        for (FORule r : rb.getRules()){
            if (unwantedSignatureF(r.getHead(),toForget)){
                forRewriting.add(r);

            }
            else if (unwantedSignatureF(r.getBody().getFormula(),toForget)) {
                toRewrite.add(r);
                forRewriting.add(r);
            }
            else {
                toKeep.add(r);
            }
        }
        Stream<FORule> newRules = rbUnfoldingWith(new RuleBaseImpl(forRewriting),toRewrite.stream()).filter(
                r -> ! unwantedSignatureF(r.getBody().getFormula(),toForget)
        );

        return new RuleBaseImpl(Stream.concat(toKeep.stream(), newRules).toList());


    }




}
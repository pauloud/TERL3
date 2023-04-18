import fr.boreal.forward_chaining.api.ForwardChainingAlgorithm;
import fr.boreal.forward_chaining.chase.ChaseBuilder;
import fr.boreal.model.formula.api.FOFormula;
import fr.boreal.model.formula.api.FOFormulaConjunction;
import fr.boreal.model.formula.impl.AtomicFOImpl;
import fr.boreal.model.formula.impl.FOConjunctionImpl;
import fr.boreal.model.kb.api.FactBase;
import fr.boreal.model.kb.api.RuleBase;
import fr.boreal.model.kb.impl.RuleBaseImpl;
import fr.boreal.model.logicalElements.api.Atom;
import fr.boreal.model.logicalElements.api.Substitution;
import fr.boreal.model.logicalElements.factory.api.TermFactory;
import fr.boreal.model.logicalElements.factory.impl.SameObjectTermFactory;
import fr.boreal.model.logicalElements.impl.SubstitutionImpl;
import fr.boreal.model.logicalElements.impl.identityObjects.IdentityConstantImpl;
import fr.boreal.model.logicalElements.impl.identityObjects.IdentityVariableImpl;
import fr.boreal.model.query.api.AtomicFOQuery;
import fr.boreal.model.query.api.FOQuery;
import fr.boreal.model.query.impl.AtomicFOQueryImpl;
import fr.boreal.model.query.impl.FOQueryConjunctionImpl;
import fr.boreal.model.rule.impl.FORuleImpl;
import fr.boreal.queryEvaluation.atomic.AtomicFOQueryEvaluator;
import fr.boreal.queryEvaluation.generic.GenericFOQueryEvaluator;
import fr.boreal.storage.inmemory.DefaultInMemoryAtomSet;
import fr.lirmm.boreal.graal.unifier.QueryUnifierAlgorithm;
import fr.boreal.model.rule.api.FORule;
import fr.lirmm.boreal.graal.unifier.QueryUnifier;
import fr.boreal.backward_chaining.pure.PureRewriterOptimized;


import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Forgetting {
    private static QueryUnifierAlgorithm qua = new QueryUnifierAlgorithm();
    private static TermFactory termFactory = SameObjectTermFactory.instance();
    private static AtomicFOQueryEvaluator evaluator = new AtomicFOQueryEvaluator();

    public static RuleBase compileRuleBase(RuleBase rb){
        return compileRuleLists(new ArrayList<> (rb.getRules()),new ArrayList<>());
    }

    private static RuleBase compileRuleLists (ArrayList<FORule> next, ArrayList<FORule> previous){
        if (next.isEmpty()){
            return new RuleBaseImpl(previous);
        }
        FORule r = next.get(0);
        next.remove(0);
        RuleBase rb = new RuleBaseImpl(Stream.concat(next.stream(),previous.stream()).toList());
        Substitution s = new SubstitutionImpl();
        r.getFrontier().stream().forEach(v -> s.add(v, new IdentityConstantImpl(v.getLabel())));
        FactBase fb = new DefaultInMemoryAtomSet(r.getBody().getFormula().flatten().stream().map(a -> s.createImageOf(a)).toList());
        Atom h = s.createImageOf(r.getHead().flatten().stream().findFirst().get());
        AtomicFOQuery q = new AtomicFOQueryImpl(new AtomicFOImpl<>(h),h.getVariables(),null);
        ForwardChainingAlgorithm chase = ChaseBuilder.defaultChase(fb, rb, termFactory);
        chase.execute();
        if (! evaluator.exist(q,fb)){
            previous.add(r);
        }

        return compileRuleLists(next,previous);

    }

    public static RuleBase rbSaturation (RuleBase rb){
        return new RuleBaseImpl (rbSaturationWith(rb,rb.getRules().stream()).toList());

    }
    private static Stream<FORule> rbSaturationWith(RuleBase rewriter, Stream<FORule> toRewrite){
        PureRewriterOptimized rw = new PureRewriterOptimized();
        return toRewrite.flatMap(r -> rw.rewrite(r.getBody(),rewriter).stream().map(
                query -> (FORule) new FORuleImpl(query,r.getHead())
        ));

    }

    public static RuleBase forget(RuleBase rb, Set<String> signature){
        Predicate<FOFormula<Atom>> unwantedSignatureF = f -> f.getPredicates().stream().anyMatch(p -> signature.contains(p.getLabel()));
        Predicate<FORule> unwantedSignatureR = r -> unwantedSignatureF.test(r.getHead())
                || unwantedSignatureF.test(r.getBody().getFormula());
        Stream<FORule> toRewrite = rb.getRules().stream();
        Stream<FORule> newRules = rbSaturationWith(rb,toRewrite);
        return new RuleBaseImpl(Stream.concat(newRules,rb.getRules().stream()).filter(Predicate.not(unwantedSignatureR)).toList());

    }

    public static FORule refreshRule(FORule rule, String suffix){
        Substitution subst = new SubstitutionImpl();
        rule.getFrontier().stream().forEach(v -> subst.add(v, new IdentityVariableImpl(v.getLabel()+suffix)));
        Collection<FOFormula<Atom>> bodyList = rule.getBody().getFormula().flatten().stream().map(
                a -> subst.createImageOf(a)
        ).map(a -> (FOFormula<Atom>) new AtomicFOImpl<Atom>(a)).toList();
        Collection<FOFormula<Atom>> headList = rule.getHead().flatten().stream().map(
                a -> subst.createImageOf(a)
        ).map(a -> (FOFormula<Atom>) new AtomicFOImpl<Atom>(a)).toList();

        FOFormulaConjunction<Atom> bodyF = new FOConjunctionImpl<>(bodyList);
        FOFormulaConjunction<Atom> head = new FOConjunctionImpl<>(headList);
        FOQuery body = new FOQueryConjunctionImpl(bodyF,bodyF.getVariables(),null);
        return new FORuleImpl(body,head);

    }

    public static FOFormula<Atom> unfoldAtom (Atom atom, FORule rule){
     return unfoldAtomFresh(atom,refreshRule(rule,"1"));
    }
    private static FOFormula<Atom> unfoldAtomFresh (Atom atom, FORule rule1){
        AtomicFOImpl<Atom> atomF= new AtomicFOImpl<>(atom);
        AtomicFOQuery query = new AtomicFOQueryImpl(atomF, atomF.getVariables(), new SubstitutionImpl());
        Iterator<QueryUnifier> unifiers = qua.getMostGeneralSinglePieceUnifiers(query,rule1).iterator();
        QueryUnifier unifier = unifiers.hasNext() ? unifiers.next() : null;
        if (unifier == null){return new FOConjunctionImpl<>(Collections.singletonList(atomF));}
        FOQuery bodyQ = rule1.getBody();
        System.out.println(unifier);
        return unifier.getImageOf(bodyQ.getFormula());
    }

    public static FORule unfold(FORule rewriter, FORule rewritten) {
       return unfoldRefresh(refreshRule(rewriter,"1"),refreshRule(rewritten,"0"));
        

    }

    private static FORule unfoldRefresh(FORule rewriter, FORule rewritten) {
        return null;
    }
    /*public static Collection<FORule> fermeture (Collection<FORule> rInit){
        Collection<FORule> rFerme = rInit;
        Collection<FORule> rNouvelles =  rInit;
        QueryUnifierAlgorithm qua = new QueryUnifierAlgorithm();
        while (! rNouvelles.isEmpty()) {
            RuleSet rProduit = new IndexedByHeadPredicatesRuleSet();
            for (FORule r1 : rNouvelles) {
                for (FORule r0 : rInit) {
                    FOFormula tete = r1.getHead();
                    Collection<QueryUnifier> unificateurs = qua.getMostGeneralSinglePieceUnifiers((FOQuery) tete,r0);
                    for (QueryUnifier unificateur : unificateurs) {
                        Substitution subst = unificateur.getAssociatedSubstitution();
                        Atom nouvelleTete = subst.createImageOf(r1.getHead()).iterator().next();
                        InMemoryAtomSet nouveauCorps = subst.createImageOf(r1.getBody());
                        nouveauCorps.addAll(subst.createImageOf(r0.getBody()));
                        nouveauCorps.removeAll(subst.createImageOf(r0.getHead()));
                        rProduit.add(new AtomicHeadRule(nouveauCorps, nouvelleTete));

                    }
                }
            }
            rFerme.addAll(rProduit.iterator());
            rNouvelles = rProduit;



        }
                    return rFerme;
                }
            }
        }*/
}
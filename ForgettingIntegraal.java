import fr.boreal.model.formula.api.FOFormula;
import fr.boreal.model.formula.api.FOFormulaConjunction;
import fr.boreal.model.formula.impl.AtomicFOImpl;
import fr.boreal.model.formula.impl.FOConjunctionImpl;
import fr.boreal.model.kb.api.RuleBase;
import fr.boreal.model.kb.impl.RuleBaseImpl;
import fr.boreal.model.logicalElements.api.Atom;
import fr.boreal.model.logicalElements.api.Substitution;
import fr.boreal.model.logicalElements.impl.SubstitutionImpl;
import fr.boreal.model.logicalElements.impl.identityObjects.IdentityVariableImpl;
import fr.boreal.model.query.api.AtomicFOQuery;
import fr.boreal.model.query.api.FOQuery;
import fr.boreal.model.query.api.FOQueryConjunction;
import fr.boreal.model.query.impl.AtomicFOQueryImpl;
import fr.boreal.model.query.impl.FOQueryConjunctionImpl;
import fr.boreal.model.rule.impl.FORuleImpl;
import fr.lirmm.boreal.graal.unifier.QueryUnifierAlgorithm;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.boreal.model.rule.api.FORule;
import fr.boreal.model.formula.impl.AtomicFOImpl;
import fr.lirmm.graphik.graal.api.core.RuleSet;
import fr.lirmm.graphik.graal.api.core.InMemoryAtomSet;
import fr.lirmm.graphik.graal.core.compilation.NoCompilation;
import fr.lirmm.graphik.graal.core.ruleset.IndexedByHeadPredicatesRuleSet;
import fr.lirmm.graphik.graal.core.unifier.AtomicHeadRule;
import fr.lirmm.boreal.graal.unifier.QueryUnifier;
import fr.boreal.backward_chaining.pure.PureRewriterOptimized;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;


import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ForgettingIntegraal {
    private static QueryUnifierAlgorithm qua = new QueryUnifierAlgorithm();

    public static RuleBase rbSaturation (RuleBase rb){
        PureRewriterOptimized rw = new PureRewriterOptimized();
        return new RuleBaseImpl(rb.getRules().stream().flatMap(r -> rw.rewrite(r.getBody(),rb).stream().map(
                query -> (FORule) new FORuleImpl(query,r.getHead())
        )).toList());

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
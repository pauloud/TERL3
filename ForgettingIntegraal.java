import fr.boreal.model.formula.api.FOFormula;
import fr.boreal.model.formula.api.FOFormulaConjunction;
import fr.boreal.model.formula.impl.AtomicFOImpl;
import fr.boreal.model.formula.impl.FOConjunctionImpl;
import fr.boreal.model.logicalElements.api.Atom;
import fr.boreal.model.logicalElements.api.Substitution;
import fr.boreal.model.logicalElements.impl.SubstitutionImpl;
import fr.boreal.model.query.api.AtomicFOQuery;
import fr.boreal.model.query.api.FOQuery;
import fr.boreal.model.query.impl.AtomicFOQueryImpl;
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


import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

public class ForgettingIntegraal {
    private static QueryUnifierAlgorithm qua = new QueryUnifierAlgorithm();
    public static FOFormula<Atom> unfoldAtom (Atom atom, FORule rule){
        AtomicFOImpl<Atom> atomF= new AtomicFOImpl(atom);
        AtomicFOQuery query = new AtomicFOQueryImpl(atomF, atomF.getVariables(), new SubstitutionImpl());
        Iterator<QueryUnifier> unifiers = qua.getMostGeneralSinglePieceUnifiers(query,rule).iterator();
        QueryUnifier unifier = unifiers.hasNext() ? unifiers.next() : null;
        if (unifier == null){return new FOConjunctionImpl<>(Collections.singletonList(atomF));}
        FOQuery bodyQ = rule.getBody();
        System.out.println(unifier);
        return unifier.getImageOf(bodyQ.getFormula());
    }
    /*public static FORule unfold(FORule headRule, FORule bodyRule) {
        Collection<Atom> body = headRule.getBody().getFormula().flatten();
        

    }*/
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
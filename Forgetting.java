import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.RuleSet;
import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.Substitution;
import fr.lirmm.graphik.graal.api.core.InMemoryAtomSet;
import fr.lirmm.graphik.graal.core.DefaultConjunctiveQuery;
import fr.lirmm.graphik.graal.core.compilation.NoCompilation;
import fr.lirmm.graphik.graal.core.ruleset.IndexedByHeadPredicatesRuleSet;
import fr.lirmm.graphik.graal.core.unifier.AtomicHeadRule;
import fr.lirmm.graphik.graal.core.unifier.QueryUnifier;
import fr.lirmm.graphik.graal.core.unifier.UnifierUtils;

import java.util.LinkedList;

public class Forgetting {
   public static RuleSet fermeture (RuleSet rInit){
       RuleSet rFerme = rInit;
       RuleSet rNouvelles =  rInit;
       while (! rNouvelles.isEmpty()){
           RuleSet rProduit = new IndexedByHeadPredicatesRuleSet();
           for (Rule r1: rNouvelles) {
               for (Rule r0 : rInit) {
                   ConjunctiveQuery tete = new DefaultConjunctiveQuery(r0.getHead());// probablement une inversion entre r1 et r0
                   LinkedList<QueryUnifier> unificateurs = UnifierUtils.getSinglePieceUnifiersAHR(tete, (AtomicHeadRule) r1, NoCompilation.instance());
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

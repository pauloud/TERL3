import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.RuleSet;
import fr.lirmm.graphik.graal.core.DefaultConjunctiveQuery;
import fr.lirmm.graphik.graal.core.compilation.NoCompilation;
import fr.lirmm.graphik.graal.core.grd.*;
import fr.lirmm.graphik.graal.core.ruleset.IndexedByHeadPredicatesRuleSet;
import fr.lirmm.graphik.graal.core.ruleset.LinkedListRuleSet;
import fr.lirmm.graphik.graal.core.unifier.AtomicHeadRule;
import fr.lirmm.graphik.graal.core.unifier.QueryUnifier;
import fr.lirmm.graphik.graal.core.unifier.UnifierUtils;

import java.util.Iterator;
import java.util.LinkedList;

public class Forgetting {
   public static RuleSet fermeture (Iterable<AtomicHeadRule> rInit){
       Iterable<AtomicHeadRule> rNouvelles =  rInit;
       while (rNouvelles.iterator().hasNext()){
           RuleSet rProduit = new IndexedByHeadPredicatesRuleSet();
           for (AtomicHeadRule r1:rNouvelles)
               for (Rule r0 : rInit) {
                   ConjunctiveQuery tete = new DefaultConjunctiveQuery(r0.getHead());
                   LinkedList<QueryUnifier> unificateurs = UnifierUtils.getSinglePieceUnifiersAHR(tete, r1, NoCompilation.instance());
               }
       }
       return null;
   }
}

import fr.boreal.model.formula.impl.AtomicFOImpl;
import fr.boreal.model.logicalElements.impl.AtomImpl;
import fr.boreal.model.logicalElements.impl.ConstantImpl;
import fr.boreal.model.logicalElements.impl.PredicateImpl;
import fr.boreal.model.logicalElements.impl.VariableImpl;
import fr.boreal.model.query.api.FOQuery;
import fr.boreal.model.query.impl.AtomicFOQueryImpl;
import fr.boreal.model.rule.impl.FORuleImpl;

public class TestForgetting {
    public static void main (String... args){
        PredicateImpl p = new PredicateImpl("p",2);
        PredicateImpl q = new PredicateImpl("q",3);
        VariableImpl X =  new VariableImpl("X");
        VariableImpl Y = new VariableImpl("Y");
        ConstantImpl a = new ConstantImpl("a");
        AtomImpl pXa = new AtomImpl(p,X, a);
        AtomImpl pYa = new AtomImpl(p,Y,a);
        AtomImpl pYX = new AtomImpl(p,Y,X);
        AtomImpl qYXa = new AtomImpl(q,Y,X,a);

        FORuleImpl r1 = new FORuleImpl(new AtomicFOQueryImpl(new AtomicFOImpl(qYXa),qYXa.getVariables(),null)
                , new AtomicFOImpl(pYX));

        System.out.println(ForgettingIntegraal.unfoldAtom(pXa,r1));
        System.out.println(ForgettingIntegraal.unfoldAtom(pYX,r1));
        System.out.println(ForgettingIntegraal.unfoldAtom(pYa,r1));
        System.out.println(ForgettingIntegraal.unfoldAtom(qYXa,r1));

    }
}

package org.yamaLab.pukiwikiCommunicator.language;
public class Fun_and implements PrimitiveFunction
{
    public ALisp lisp;
    public Fun_and(ALisp l)
    {
        lisp=l;
    }
    public final LispObject fun(LispObject proc, LispObject argl)
    {
        return lisp.and(lisp.car(argl),lisp.second(argl));
    }
}


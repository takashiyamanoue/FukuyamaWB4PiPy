package org.yamaLab.pukiwikiCommunicator.language;
public class Fun_or implements PrimitiveFunction
{
    public ALisp lisp;
    public Fun_or(ALisp l)
    {
        lisp=l;
    }
    public final LispObject fun(LispObject proc, LispObject argl)
    {
        return lisp.or(lisp.car(argl),lisp.second(argl));
    }
}


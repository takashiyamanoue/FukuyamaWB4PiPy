package org.yamaLab.pukiwikiCommunicator.language;
public class Fun_not implements PrimitiveFunction
{
    public ALisp lisp;
    public Fun_not(ALisp l)
    {
        lisp=l;
    }
    public LispObject fun(LispObject proc, LispObject argl)
    {
                LispObject x=(lisp.car(argl));
                return lisp.not(x);
    }
}


% NotMember function
notMember(E, L) :- \+ member(E, L).

% Suitable node
suitable(X, Y, Path) :- node(c(X, Y)), notMember(c(X, Y), Path).

% Random functions
upperProbability(P) :- rand_int(100, R), R >= P.
lowerProbability(P) :- rand_int(100, R), R =< P.
enoughConsecutive(Cons, Cons2, Max) :- Delta is 100 / Max, Prob is Cons * Delta, upperProbability(Prob), Cons2 is Cons+1.
notEnoughConsecutive(Cons, Max):- Delta is 100 / Max, Prob is Cons * Delta, lowerProbability(Prob).

% Random path from (X0, Y0) to (XF, YF)
path(c(X0, Y0), c(XF, YF), P):-path(c(X0, Y0), c(XF, YF), P, [], 1, 0).
path(c(X,Y), c(X,Y), [c(X,Y)|[]], Acc, _, _).
path(c(X0, Y0), c(XF, YF), [c(X0,Y0)|P], Acc, ConsX, ConsY):- ConsY > 3, ConsX=<0, enoughConsecutive(ConsX, _, 3), ConsX2 is ConsX-1, ConsY2 is ConsY-1, X1 is X0-1, X1>=0, suitable(X1, Y0, Acc), path(c(X1,Y0), c(XF,YF), P, [c(X0,Y0)|Acc], ConsX2, ConsY2).
path(c(X0, Y0), c(XF, YF), [c(X0,Y0)|P], Acc, ConsX, ConsY):- enoughConsecutive(ConsY, ConsY2, 5), Y1 is Y0+1, maxY(Y1), suitable(X0, Y1, Acc), path(c(X0,Y1), c(XF,YF), P, [c(X0,Y0)|Acc], 0, ConsY2).
path(c(X0, Y0), c(XF, YF), [c(X0,Y0)|P], Acc, ConsX, ConsY):- enoughConsecutive(ConsY, ConsY2, 5), Y1 is Y0-1, Y1>=0, suitable(X0, Y1, Acc), path(c(X0,Y1), c(XF,YF), P, [c(X0,Y0)|Acc], 0, ConsY2).
path(c(X0, Y0), c(XF, YF), [c(X0,Y0)|P], Acc, ConsX, ConsY):- ConsX2 is ConsX+1, ConsY2 is ConsY-1, X1 is X0+1, maxX(X1), suitable(X1, Y0, Acc), path(c(X1,Y0), c(XF,YF), P, [c(X0,Y0)|Acc], ConsX2, ConsY2).

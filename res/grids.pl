% NotMember function
notMember(E, L) :- \+ member(E, L).

% Node structure
node(X, Y):- node(c(X,Y)).
node(c(X,Y)):-member(X,[0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15]),member(Y,[0,1,2,3,4,5,6,7]).

% Suitable node
suitable(X, Y, Path) :- node(X, Y), notMember(c(X, Y), Path).

% Random functions
probability(P) :- rand_int(100, R), R >= P.
enoughConsecutive(Cons, Cons2, Max) :- Delta is 100 / Max, Prob is Cons * Delta, probability(Prob), Cons2 is Cons+1.

% All path
allPath(c(X0, Y0), c(XF, YF), P):-allPath(c(X0, Y0), c(XF, YF), P, [], 1, 0).
allPath(c(X,Y), c(X,Y), [c(X,Y)|[]], Acc, _, _).
allPath(c(X0, Y0), c(XF, YF), [c(X0,Y0)|P], Acc, ConsX, ConsY):- enoughConsecutive(ConsX, _, 3), enoughConsecutive(ConsY, ConsY2, 5), Y1 is Y0+1, Y1=<7, suitable(X0, Y1, Acc), allPath(c(X0,Y1), c(XF,YF), P, [c(X0,Y0)|Acc], 0, ConsY2).
allPath(c(X0, Y0), c(XF, YF), [c(X0,Y0)|P], Acc, ConsX, ConsY):- enoughConsecutive(ConsX, _, 3), enoughConsecutive(ConsY, ConsY2, 5), Y1 is Y0-1, Y1>=0, suitable(X0, Y1, Acc), allPath(c(X0,Y1), c(XF,YF), P, [c(X0,Y0)|Acc], 0, ConsY2).
allPath(c(X0, Y0), c(XF, YF), [c(X0,Y0)|P], Acc, ConsX, ConsY):- enoughConsecutive(ConsX, ConsX2, 3), X1 is X0+1, X1=<XF, suitable(X1, Y0, Acc), allPath(c(X1,Y0), c(XF,YF), P, [c(X0,Y0)|Acc], ConsX2, 0).

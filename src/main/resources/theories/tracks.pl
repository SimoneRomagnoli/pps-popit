
% notMember(?Element, ?List)
% Succeeds if Element does not unify with a member of the list List.
notMember(E, L) :- \+ member(E, L).

% suitable(?X, ?Y, ?Path)
% Succeeds if the node c(X, Y) is a node of the game grid and if it is not a member of Path.
suitable(X, Y, Path) :- node(c(X, Y)), notMember(c(X, Y), Path).

% upperProbability(@Probability)
% Succeeds if Probability is lower than a random number.
upperProbability(P) :- rand_int(100, R), R >= P.

% lowerProbability(@Probability)
% Succeeds if Probability is greater than a random number.
lowerProbability(P) :- rand_int(100, R), R =< P.

% enoughConsecutive(+Cons, -Cons2, +Max)
% The higher Max is, the higher is the probability that this can succeed.
enoughConsecutive(Cons, Cons2, Max) :- Delta is 100 / Max, Prob is Cons * Delta, upperProbability(Prob), Cons2 is Cons+1.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%                                                                      %
%                    POP-IT PATH FINDING ALGORITHM                     %
%                                                                      %
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% path(c(+X0, +Y0), c(+XF, +XF), @Difficulty, -Path)
% Finds a random Path starting from cell (X0, Y0) and ending in cell (XF, YF);
% the Difficulty determines the length and the complexity of the path, that could have more straight road segments.
% The first two predicates determine the start of the computation and the finish;
% each of the remaining predicates, determines a movement towards one of the four directions.

% Start the algorithm adding an empty accumulator, the counter for consecutive X movements set to 1 and the counter for consecutive Y movements set to 0.
path(c(X0, Y0), c(XF, YF), D, P) :- path(c(X0, Y0), c(XF, YF), D, P, [], 1, 0).

% The algorithm ends when the current cell unifies with the last one.
path(c(X,Y), c(X,Y), D, [c(X,Y)|[]], Acc, _, _).

% This predicate determines a LEFT movement;
% it is possible only if the difficulty of the game is not hard.
path(c(X0, Y0), c(XF, YF), D, [c(X0,Y0)|P], Acc, ConsX, ConsY):- D < 3, ConsY > 3, ConsX=<0, Max is D+2, enoughConsecutive(ConsX, _, Max),
                                                                    ConsX2 is ConsX-1, ConsY2 is ConsY-1, X1 is X0-1, X1>=0, suitable(X1, Y0, Acc),
                                                                    path(c(X1,Y0), c(XF,YF), D, P, [c(X0,Y0)|Acc], ConsX2, ConsY2).

% This predicate determines a DOWN movement.
path(c(X0, Y0), c(XF, YF), D, [c(X0,Y0)|P], Acc, ConsX, ConsY):- Max is 7-D, enoughConsecutive(ConsY, ConsY2, Max),
                                                                    Y1 is Y0+1, maxY(Y1), suitable(X0, Y1, Acc),
                                                                    path(c(X0,Y1), c(XF,YF), D, P, [c(X0,Y0)|Acc], 0, ConsY2).

% This predicate determines a UP movement.
path(c(X0, Y0), c(XF, YF), D, [c(X0,Y0)|P], Acc, ConsX, ConsY):- Max is 7-D, enoughConsecutive(ConsY, ConsY2, Max),
                                                                    Y1 is Y0-1, Y1>=0, suitable(X0, Y1, Acc),
                                                                    path(c(X0,Y1), c(XF,YF), D, P, [c(X0,Y0)|Acc], 0, ConsY2).

% This predicate determines a RIGHT movement.
path(c(X0, Y0), c(XF, YF), D, [c(X0,Y0)|P], Acc, ConsX, ConsY):- ConsX2 is ConsX+1, ConsY2 is ConsY-1, X1 is X0+1, maxX(X1), suitable(X1, Y0, Acc),
                                                                    path(c(X1,Y0), c(XF,YF), D, P, [c(X0,Y0)|Acc], ConsX2, ConsY2).

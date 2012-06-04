% The alpha-beta algorithm

alphabeta( Pos, Alpha, Beta, GoodPos, Val)  :-
  moves( Pos, PosList), !,
  boundedbest( PosList, Alpha, Beta, GoodPos, Val);
  staticval( Pos, Val).                              % Static value of Pos 

boundedbest( [Pos | PosList], Alpha, Beta, GoodPos, GoodVal)  :-
  alphabeta( Pos, Alpha, Beta, _, Val),
  goodenough( PosList, Alpha, Beta, Pos, Val, GoodPos, GoodVal).

goodenough( [], _, _, Pos, Val, Pos, Val)  :-  !.    % No other candidate

goodenough( _, Alpha, Beta, Pos, Val, Pos, Val)  :-
  min_to_move( Pos), Val > Beta, !                   % Maximizer attained upper bound
  ;
  max_to_move( Pos), Val < Alpha, !.                 % Minimizer attained lower bound

goodenough( PosList, Alpha, Beta, Pos, Val, GoodPos, GoodVal)  :-
  newbounds( Alpha, Beta, Pos, Val, NewAlpha, NewBeta),    % Refine bounds  
  boundedbest( PosList, NewAlpha, NewBeta, Pos1, Val1),
  betterof( Pos, Val, Pos1, Val1, GoodPos, GoodVal).

newbounds( Alpha, Beta, Pos, Val, Val, Beta)  :-
  min_to_move( Pos), Val > Alpha, !.                 % Maximizer increased lower bound 

newbounds( Alpha, Beta, Pos, Val, Alpha, Val)  :-
   max_to_move( Pos), Val < Beta, !.                 % Minimizer decreased upper bound 

newbounds( Alpha, Beta, _, _, Alpha, Beta).          % Otherwise bounds unchanged 

% Minimax

minimax( Pos, BestSucc, Val)  :-
  moves( Pos, PosList), !,               % Legal moves in Pos produce PosList
  best( PosList, BestSucc, Val)
   ;
  staticval( Pos, Val).                 % Pos has no successors: evaluate statically 

best( [ Pos], Pos, Val)  :-
  minimax( Pos, _, Val), !.

best( [Pos1 | PosList], BestPos, BestVal)  :-
  minimax( Pos1, _, Val1),
  best( PosList, Pos2, Val2),
  betterof( Pos1, Val1, Pos2, Val2, BestPos, BestVal).

betterof( Pos0, Val0, Pos1, Val1, Pos0, Val0)  :-        % Pos0 better than Pos1
  min_to_move( Pos0),                                    % MIN to move in Pos0
  Val0 > Val1, !                                         % MAX prefers the greater value
  ;
  max_to_move( Pos0),                                    % MAX to move in Pos0
  Val0 < Val1, !.                                % MIN prefers the lesser value 

betterof( Pos0, Val0, Pos1, Val1, Pos1, Val1).           % Otherwise Pos1 better than Pos0
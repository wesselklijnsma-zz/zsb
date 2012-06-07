% -*- Mode: Prolog -*-

% Position is represented by Side..Wx : Wy..Qx : Qy .. Bx : By .. Depth
% Side is side to move next ( us or them )
% Wx, Wy are X and Y coordinates of the white king
% Qx, Qy are X and Y coordinates of the white queen
% Bx, By are the X and Y coordinates of the black king
% depth is depth of position in the search tree

mode(queen).

% call the general original move predicates for queen moves etc.
move(A,B,C,D):-
        moveGeneral(A,B,C,D).

move( queenmove, us..W..Qx:Qy..B..D, Qx:Qy - Q, them..W..Q..B..D1 ) :-
	D1 is D + 1,
	coord(I), % integer between 1 and 8
	X is I - Qx, % diagonal move X
	Y is I - Qy, % diagonal move Y
	(
		Q = I : Qy % horizontal move
		;
		Q = Qx : I % vertical move
		;
		Q = X : Y  % diagonal move	
	),

	Q \= Qx:Qy, % queen must have moved
	Q \= W,	% white king can't occupy space	
	Q \= B,	% black king can't occupy space
	not inway(Qx:Qy, W, Q). % white king not inway

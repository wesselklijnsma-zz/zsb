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
	(
		Q = I : Qy % horizontal move
		;
		Q = Qx : I % vertical move
		;
		(
			X is I - Qx,
			Y is I - Qy,
			X > 0,
			Y > 0,
			Q = X : Y  % diagonal move	
		)
	),

	Q \= Qx:Qy, % queen must have moved
	Q \= W,	% white king can't occupy space	
	Q \= B,	% black king can't occupy space
	not inway(Qx:Qy, W, Q), % white king not inway
	not inway(Qx:qy, B, Q). % black ' ' ' 

move( legal, us..P, M, P1 ) :-
	(
		MC = kingdiagfirst
	;
		MC = queenmove
	),
	move( MC, us..P, M, P1 ).

did_not_move_queen(Pos, Root) :-
	wp(Pos, Q1),
	wp(Root, Q2),
	
	Q1 = Q2.

queenlost( _.._W..B..B.._ ,_).	% queen has been captured

queenlost( them..W..Q..B.._ ,_) :-
	ngb( B, Q ),	% black king attacks queen
	not ngb( W, Q ).	% white king does not defend

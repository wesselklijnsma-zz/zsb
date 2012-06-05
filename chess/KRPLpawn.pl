% -*- Mode: Prolog -*-

% Position is represented by Side..Wx : Wy..Px : Py .. Bx : By .. Depth
% Side is side to move next ( us or them )
% Wx, Wy are X and Y coordinates of the white king
% Px, Py are X and Y coordinates of the white pawn
% Bx, By are the X and Y coordinates of the black king
% depth is depth of position in the search tree

mode(pawn).

% call the general original move predicates for pawn moves etc.
move(A,B,C,D):-
        moveGeneral(A,B,C,D).

move(pawnmove, us..W..Px : Py..B..D, Px:Py - P, them..W..P..B..D1 ) :-
	D1 is D + 1,

	P is Py + 1,

	P \= Px:Py,
	P \= W,
	P \= B.

move(promotionmove, Pos, Px:Py - P, Pos1 ) :-
 	wp( Pos, Px:7 ),

 	P = Px:8,
	move(pawnmove, Pos, Px:Py - P, Pos1).
	
pawnlost(_.._W..B..B.._,_).

pawnlost( them..W..P..B.._,_) :-
	ngb( B, P),
	not ngb( W, P).   	

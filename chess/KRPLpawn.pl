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

move(pawnmove, us..W..Px:Py..B..D, Px:Py - P, them..W..P..B..D1 ) :-
	D1 is D + 1,

	NPy is Py + 1, % moves pawn up one place
	P = Px:NPy,
	P \= Px:Py, % must move
	P \= W, % can't move to occupied place
	P \= B. % ''

move(legal, us..P, M, P1) :-
	( MC = kingdiagfirst
		;
	  MC = pawnmove),
	move(MC, us..P, M, P1). 

pawnlost(_.._W..B..B.._,_).

pawnlost( them..W..P..B.._,_) :-
	ngb( B, P), %black king attacks pawn
	not ngb( W, P).   %white king doesn't defend

pawnpromoted(Pos, _) :-
	wp( Pos, _:8 ).

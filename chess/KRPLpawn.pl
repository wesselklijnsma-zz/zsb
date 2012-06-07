%
% Names       : Jouke van der Maas & Wessel Klijnsma
% Student IDs : 10186883 and 10172432
% Description : Part of solution to King and Pawn vs. Pawn chess problem,
%		describes movements of the pawn.
% Date        : June 2012
% Comments    : This the structure of this file is based on KRPLrook.pl from 
%		Prolog Programming for Artificial Intelligence by Ivan Bratko.
%

% -*- Mode: Prolog -*-

% Position is represented by Side..Wx : Wy..Px : Py .. Bx : By .. Depth
% Side is side to move next ( us or them )
% Wx, Wy are X and Y coordinates of the white king
% Px, Py are X and Y coordinates of the white pawn
% Bx, By are the X and Y coordinates of the black king
% depth is depth of position in the search tree

mode(pawn).

% Call the original move predicate from KRPL.pl
move(A,B,C,D):-
        moveGeneral(A,B,C,D).

% The only legal pawnmove is straight forward (there is no attacking when
% there's just a king). 
move(pawnmove, us..W..Px:Py..B..D, Px:Py - P, them..W..P..B..D1 ) :-
	D1 is D + 1,

	NPy is Py + 1, % moves pawn up one place
	P = Px:NPy,
	P \= Px:Py, % must move
	P \= W, % can't move to occupied place
	P \= B.

% Define legal moves. Since there's only a king and a pawn, theirs are the
% only  legal moves.
move(legal, us..P, M, P1) :-
	( MC = kingdiagfirst
		;
	  MC = pawnmove),
	move(MC, us..P, M, P1). 

% If the black king occupies the same space as the white pawn, it has been
% taken.
pawnlost(_.._W..B..B.._,_).

% One move in advance; if the black king attacks the pawn and the white king
% isn't defend, the pawn is lost.  
pawnlost( them..W..P..B.._,_) :-
	ngb( B, P),
	not ngb( W, P).

% The pawn is promoted if it reaches the last row.
pawnpromoted(Pos, _) :-
	wp( Pos, _:8 ).

% The pawn is closer to its promotion if it has moved closer to the last row
% (if its y coordinate has increased).
closertopromotion( Pos, Root ) :-
	wp( Pos, _:Py ),
	wp( Root, _:RPy),
	Py > RPy.

% The white king is closer to a 'critical' square if the distance between its
% position and the square is getting smaller.
closertocritical( Pos, Root ) :-
	wp( Pos, Px:Py ),
	wk( Pos, WK ), 
	wk( Root, RootWK ), 

	critsquare( Px:Py, WK, C ), 
	dist( WK, C, KD ),
	dist( RootWK, C, RootKD ),
	RootKD > KD.

% If the black king is anywhere in this square or can enter it in one move, 
% it can attack the pawn before promotes. Note: the 'square' is really two 
% squares that share one side, and the black king can be in one of them at a time.
bk_in_square( Pos , _ ) :-
	wp( Pos, Px:Py ),
	Side is 8 - Py,
	bk( Pos, BK ),

	ngb( BK, NKx:NKy ),
	NKy > 8 - Side - 1,
	NKx =< Px + Side,
	NKx >= Px - Side.

% True if the white king is directly adjacent to the white pawn and they have
% the same x coordinate.
wk_on_critical( Pos, _ ) :-
	wp(Pos, WP),
    	wk(Pos, WK),
    	critsquare(WP, WK, WK).

% Any square directly adjacent and with the same x coordinate as the white pawn
% is a critical square. Note: white king's coordinates are used to ensure the king
% always moves towards the closest crit square.
critsquare( Px:Py, Kx:_, Cx:Py) :-
	Kx >= Px,
	Cx is Px + 1.
critsquare( Px:Py, Kx:_, Cx:Py) :-
	Kx < Px,
	Cx is Px - 1.

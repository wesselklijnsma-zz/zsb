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
pawn_lost(_.._W..B..B.._,_).

% One move in advance; if the black king attacks the pawn and the white king
% isn't defend, the pawn is lost.  
pawn_lost( them..W..P..B.._,_) :-
	ngb( B, P),
	not ngb( W, P).

% The pawn is promoted if it reaches the last row.
pawn_promoted(Pos, _) :-
	wp( Pos, _:8 ).

% The pawn is closer to its promotion if it has moved closer to the last row
% (if its y coordinate has increased).
closer_to_promotion( Pos, Root ) :-
	wp( Pos, _:Py ),
	wp( Root, _:RPy),
	Py > RPy.

% Even if the pawn is promoted in its new position, we need to make sure it
% isn't lost.
promotion_possible( Pos, _ ) :-
	move(pawnmove, Pos, _, Pos1),
	pawn_promoted(Pos1, _),
	\+ pawn_lost(Pos1, _).

% The white king is closer to a 'critical' square if the distance between its
% position and the square is getting smaller.
closer_to_critical( Pos, Root ) :-
	wp( Pos, Px:Py ),
	wk( Pos, WK ), 
	wk( Root, RootWK ), 

	crit_square( Px:Py, C ), 
	dist( WK, C, KD ),
	dist( RootWK, C, RootKD ),
	RootKD > KD.

% Only check for pawnmoves; the king never causes trouble.
possible_stalemate( Pos, _ ) :-
	move(pawnmove, Pos, _, Pos1),
	stalemate(Pos1, _ ).

% The pawn is protected by the white king.
pawn_protected( Pos, _ ) :-
	wk( Pos, WK ),
	wp( Pos, WP ),
	ngb( WK, WP ).

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
    	crits_quare(WP, WK).

% If two positions are the same, nothing happened.
did_not_move( Pos, Pos ).

% The squares directly diagonal and in front of the white pawn are critical squares.
% Note: there are more optimal critical squares, but a more complex strategy is needed
% to make proper use of them. The squares defined here work in most situations.
crit_square( Px:Py, Cx:Cy) :-
	(Cx is Px + 1;
	Cx is Px - 1),
	in(Cx),
	Cy is Py + 1,
	in(Cy).

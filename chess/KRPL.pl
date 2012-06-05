% -*- Mode: Prolog -*-
% $Id: KRPL.pl,v 1.14 2008/06/10 11:10:53 obooij Exp $
% from Prolog Programming for AI, Bratko p. 604

% Predicate library for king and rook vs king

% Position is represented by Side..Wx : Wy..Px : Py .. Bx : By .. Depth
% Side is side to move next ( us or them )
% Wx, Wy are X and Y coordinates of the white king
% Px, Py are X and Y coordinates of the white rook
% Bx, By are the X and Y coordinates of the black king
% depth is depth of position in the search tree

% selector relations needed 

side( Side.._, Side ).			% side to move in position
wk( _..WK.._, WK ).			% white king coordinate
wp( _.._..WP.._, WP ).			% white rook coordinates
bk( _.._.._..BK.._, BK ).		% black king coordinates
depth( _.._.._.._..Depth, Depth ).	% depth of position in search tree

resetdepth( S..W..P..B.._D, S..W..P..B..0 ). 	% copy of position with depth 0

% basic operation
max( A, B, A ) :-
	A > B, !.

max( A, B, A ) :-
	A = B.

max( A, B, B ) :-
	B > A, !.

max( A, B, B ) :-
	A = B.

ordered( A, B, C ) :-
	A >= B, B >= C.

ordered( A, B, C ) :-
	A =< B, B =< C.

orderedBoth( Ax:Ay, Bx:By, Cx:Cy ) :-
	ordered( Ax, Bx, Cx ),
	ordered( Ay, By, Cy ).


% some relations between squares
n( N, N1 ) :-
	( N1 is N + 1
	;
	  N1 is N - 1
	),
	in( N1 ).

in( N ) :-
	N > 0, 
      	N < 9.

diagngb( X : Y, X1 : Y1 ) :-
	n( X, X1 ), 
	n( Y, Y1 ).

verngb( X : Y, X : Y1 ) :-
	n( Y, Y1 ).

horngb( X : Y, X1 : Y ) :-
	n( X, X1 ).

ngb( S, S1 ) :-
	diagngb( S, S1 ).

ngb( S, S1 ) :-
	horngb( S, S1 ).

ngb( S, S1 ) :-
	verngb( S, S1 ).

end_of_game( Pos ):-
	mate( Pos ).

coord(1).
coord(2).
coord(3).
coord(4).
coord(5).
coord(6).
coord(7).
coord(8).

% move constraints predicates
% these are specialized move generators:
% move( moveconstraint, pos, move, newpos )

moveGeneral( depth < Max, Pos, _Move, _Pos1 ) :-
	%write( '  move depth < Max ' ), 
	depth( Pos, D ),
	%write( D ), write( '<' ), write( Max ), nl,
	D < Max, !.

moveGeneral( depth = D, Pos, _Move, _Pos1 ) :-
	depth( Pos, D ), !.

moveGeneral( kingdiagfirst, us..W..P..B..D, W-W1, them..W1..P..B..D1 ):-
	D1 is D + 1,
	ngb( W, W1 ),		% ngb creates diagonal moves first
	not ngb( W1, B ),	% Must not move into check
	W1 \== P.		% Must not collide with rook

moveGeneral( legal, them..W..P..B..D, B-B1, us..W..P..B1..D1 ) :-
	D1 is D + 1,
	ngb( B, B1 ),
	not check( us..W..P..B1..D1 ).

legalmove( Pos, Move, Pos1 ):-
	move( legal, Pos, Move, Pos1 ).

% black king next to white king
check( _US..W.._P..B.. _D ) :-	
	ngb( W, B ).

% pawn is directly diagonal
check( _US..W..Px : Py..Bx : By.. _D ) :-	
	( Px is Bx - 1
	  ;
	  Px is Bx + 1
	),
        Py is By - 1.

% piece on the destination spot 
inway( _S, S1, S1 ) :- !.

%horizontal
inway( X1:Y, X2:Y, X3:Y ) :-
	ordered( X1, X2, X3 ).

%vertical
inway( X:Y1, X:Y2, X:Y3 ) :-
	ordered( Y1, Y2, Y3 ).

%diagonal, moving left-bottom to right-top
inway( X1:Y1, X2:Y2, X3:Y3 ) :-
  orderedBoth( X1:Y1, X2:Y2, X3:Y3 ),
  D1 is X1-Y1,
  D2 is X2-Y2,
  D3 is X3-Y3,
  D1 is D2,
  D2 is D3.

%diagonal, moving right-bottom to left-top
inway( X1:Y3, X2:Y2, X3:Y1 ) :-
  orderedBoth( X1:Y1, X2:Y2, X3:Y3 ),
  D1 is X1-Y1,
  D2 is X2-Y2,
  D3 is X3-Y3,
  D1 is D2,
  D2 is D3.

% goal predicates
true( _Pos ).
true( _Pos, _RootPos ).

themtomove( them.._ ,_ ).

mate( Pos , _ ) :-
        mate(Pos).

mate( Pos ) :-
	side( Pos, them ),
	check( Pos ),		% black king is checked
	not legalmove( Pos, _, _ ).	% black can play no valid move

stalemate( Pos, _ ) :-
	side( Pos, them ),
	%write( ' Stalemate??? ' ),nl,
	not check( Pos ),	% black king is not checked
	not legalmove( Pos, _, _ ),
	write( 'Stalemate' ).	% black can play no valid move

newroomsmaller( Pos, RootPos ) :-
	room( Pos, Room ),
	room( RootPos, RootRoom ),
	Room < RootRoom.

okapproachedsquare( Pos, RootPos ) :-
	okcsquaremdist( Pos, D1 ),
	okcsquaremdist( RootPos, D2 ),
	D1 < D2.

okcsquaremdist( Pos, MDist ) :- % Manhattan distance between WK and critical square
	wk( Pos, WK ),
	cs( Pos, CS ),
	manhdist( WK, CS, MDist ).

lpatt( _..W..P..B.._ ,_) :-	% L-pattern
	manhdist( W, B, 2 ),
	manhdist( P, B, 3 ).

okorndle( _Side..W..P.._B.._D, _Side1..W1..P1.._B1.._D1 ) :-
	dist( W, P, D ),
	dist( W1, P1, D1 ),
	D =< D1.

roomgt2(  Pos, _ ) :-
	room( Pos, Room ),
	%write( 'Room > 2? ' ), write( Room ), nl,
	Room > 2.

our_king_edge( _..1:_Y.._.._.._ , _ ):- !.
our_king_edge( _..8:_Y.._.._.._ , _ ):- !.
our_king_edge( _.._X:1.._.._.._ , _ ):- !.
our_king_edge( _.._X:8.._.._.._ , _ ):- !.

their_king_edge( _.._.._..1:_Y.._ ,_):- !.
their_king_edge( _.._.._..8:_Y.._ ,_):- !.
their_king_edge( _.._.._.._X:1.._ ,_):- !.
their_king_edge( _.._.._.._X:8.._ ,_):- !.

kings_close( Pos , _ ) :-
        kings_close( Pos ).

kings_close( Pos ) :-
	wk( Pos, WK ),
	bk( Pos, BK ),
	dist( WK, BK, D ),
	D < 4.

king_in_square( Pos , _ ) :-
	wp( Pos, Px:Py ),
	Size is 8 - Py,
	
	move(kingdiagfirst, Pos, _, Pos1),
	bk( Pos1, NKx:NKy ),
	NKy > 8 - Size - 1,
	NKx =< Px + Size,
	NKx >= Px - Size. 
	
king_blocked( Pos , _ ) :-
	wp(Pos, Px:_),
	wk(Pos, WK),
	bk(Pos, BK),
	inway(WP, Goal, Px:8),
	inway(Goal, WK, BK).

dist( X : Y, X1: Y1, D ) :-
	absdiff( X, X1, Dx ),
	absdiff( Y, Y1, Dy ),
	max( Dx, Dy, D ).

absdiff( A, B, D ) :-
	A > B, !,
	D is A - B.

absdiff( A, B, D ) :-
	D is B - A.

manhdist( X : Y, X1: Y1, D ) :-	% Manhattan distance
	absdiff( X, X1, Dx ),
	absdiff( Y, Y1, Dy ),
	D is Dx + Dy.

room( Pos, Room ):-		% area to which the black king is confined
	wp( Pos, Px : Py ),
	bk( Pos, Bx : By ),
	( Bx < Px, SideX is Px - 1
	  ;
	  Bx > Px, SideX is 8 - Px
	),
	(
	  By < Py, SideY is Py - 1
	  ;
	  By > Py, SideY is 8 - Py
	),
	Room is SideX * SideY, !.

room( _, 64 ).		% rook in line with black king

cs( _.._W.. Px : Py .. Bx : By .. _, Cx:Cy ) :-
	( Bx < Px, !, Cx is Px - 1
	  ;
	  Cx is Px + 1
	),
	( By < Py, !, Cy is Py - 1
	  ;
	  Cy is Py + 1
	).

	
% display procedures
% show the board setting on screen.
% (this does not show the position in a format gnuchessr can read (TODO))
show( Pos ) :-
	coord( Y ), nl,
	YUpDown is 9-Y,
	write( YUpDown ),
	write( '|' ),
	coord( X ),
	writepiece( X : YUpDown, Pos ),
	fail.

show( Pos ) :-
	nl, write( ' +----------------' ), nl,
   %nl, write( ' |________________' ), nl,
	write( '  1 2 3 4 5 6 7 8' ), nl,
	side( Pos, S ), 
	depth( Pos, D ),
	nl, write( 'Side=' ), write( S ),
	write( 'Depth=' ), write( D ), nl.

% white king
writepiece( Square, Pos ):-
	wk( Pos, Square ), 
		not mode(rookrook), !,
	write( 'k ' ).

% white rook 2 (instead of king)
writepiece( Square, Pos ):-
	wk( Pos, Square ), 
		mode(rookrook),!,
	write( 'r ' ).

% white rook
writepiece( Square, Pos ):-
	wp( Pos, Square ),
        mode(rook), !,
	write( 'r ' ).

% white queen
writepiece( Square, Pos ):-
	wp( Pos, Square ),
        mode(queen), !,
	write( 'q ' ).

% white pawn
writepiece( Square, Pos ):-
	wp( Pos, Square ),
        mode(pawn), !,
	write( 'p ' ).

% black king
writepiece( Square, Pos ):-
	bk( Pos, Square ), !,
	write( 'K ' ).

% emtpy space
writepiece( _Square, _Pos ):-
	write( '. ' ).

% ugly clause to write the move to move.txt
savemove( X:Y-X2:Y2 ):-
	tell( 'move.txt' ),
	% process characters
	C1 is X + 96,
	%C2 is 57 - Y, % this flips the Y-positions... 
	C2 is 48 + Y, % this doesn't
	C3 is X2 + 96,
	%C4 is 57 - Y2, % this flips the Y-positions... 
	C4 is 48 + Y2, % this doesn't

	% write them to file
	put( C1 ),
	put( C2 ),
	put( C3 ),
	put( C4 ),

	% finished writing
	told.

diag(A,B):-
        coord(I),
        diag(A,B,I).

diag(X0:Y0,X1:Y1,I):-
        ( I0=I,
          I1=I
          ;
          I0 is -1*I,
          I1 is -1*I
          ;
          I0=I,
          I1 is -1*I
          ;
          I0 is -1*I,
          I1=I
        ),
        plus(X0,I0,X1),
        plus(Y0,I1,Y1),
        in(X0),
        in(X1),
        in(Y0),
        in(Y1).


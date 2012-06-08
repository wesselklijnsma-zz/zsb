% -*- Mode: Prolog -*-
%
% Names       : Jouke van der Maas & Wessel Klijnsma
% Student IDs : 10186883 and 10172432
% Description : Part of solution to King and Pawn vs. Pawn chess problem,
%		describes the strategy for promoting the pawn.
% Date        : June 2012
% Comments    : This the structure of this file is based on KRAProok.pl from 
%		Prolog Programming for Artificial Intelligence by Ivan Bratko.
%

% King and Pawn vs king in Advice Language 0

% all rules

% Prevent moves that cause stalemate.
stale_rule :: if possible_stalemate
	then [ expel_bk, any_move ].

% Promote the pawn in one move if possible.
promotion_rule :: if promotion_possible
	then [ promote_pawn ].

% If the pawn is free to move, it should try to get promoted.
square_rule :: if wk_on_critical or not bk_in_square 
	then [ promote_pawn, any_move ].

% If the pawn is not free to move, get the white king to protect it.
blocking_rule :: if not wk_on_critical
	then [ block_king, move_to_critical, any_move ].

% If all else fails, just to any move.
else_rule :: if true
	then [ any_move ].

% pieces of advice
% structure:
% advice( NAME, BETTERGOAL: HOLDINGGOAL: USMOVECONSTRAINT:
%		THEMMOVECONSTRAINT)

% Move the white king somewhere around the white pawn to force the
% black king to move away.
advice( expel_bk,
	not possible_stalemate :
	pawn_protected and not pawn_lost :
	(depth = 0) and kingdiagfirst :
	nomove ). 

% Move the pawn towards the end line.
advice( promote_pawn,
	pawn_promoted or closer_to_promotion :
	not pawn_lost :
	(depth = 0) and pawnmove :
	nomove ).

% Move the white king to a square directly diagonal and in front of
% the white pawn to prevent the black king from taking it.
advice( block_king,
	wk_on_critical :
	not pawn_lost :
	(depth = 0) and kingdiagfirst :
	nomove ).

% If the king is already protecting the pawn, it should keep doing so while
% it moves to a new critical square. If it was not already protecting the
% pawn, the holding goal pawn_protected will fail and the king will just
% try to get to the pawn using the second predicate.
advice( move_to_critical,
	closer_to_critical :
	pawn_protected and not pawn_lost : 
	(depth = 0) and kingdiagfirst :
	nomove ).
advice( move_to_critical,
	closer_to_critical :
	not pawn_lost :
	(depth = 0) and kingdiagfirst :
	nomove ).

% If possible, keep the pawn protected and don't let it get taken. If this is
% not possible, try just to not get it taken. If that fails too, just make any
% move; we've already lost the race at that point anyway.
advice( any_move,
	not did_not_move :
	pawn_protected and not pawn_lost :
	(depth = 0) and (pawnmove or kingdiagfirst) :
	nomove ).
advice( any_move,
	not did_not_move :
	not pawn_lost :
	(depth = 0) and (pawnmove or kingdiagfirst) :
	nomove ).
advice( any_move,
	not did_not_move :
	not did_not_move :
	(depth = 0) and (pawnmove or kingdiagfirst) :
	nomove ).

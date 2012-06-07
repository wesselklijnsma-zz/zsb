% -*- Mode: Prolog -*-
% King and Pawn vs king in Advice Language 0

% all rules

square_rule :: if wk_on_critical or not bk_in_square
	then [ promote_pawn, race_pawn, any_move ].
blocking_rule :: if not wk_on_critical
	then [ block_king, move_to_critical, any_move ].
else_rule :: if true
	then [ any_move ].

% pieces of advice
% structure:
% advice( NAME, BETTERGOAL: HOLDINGGOAL: USMOVECONSTRAINT:
%		THEMMOVECONSTRAINT)


advice( race_pawn,
	closertopromotion :
	not pawnlost :
	(depth = 0) and pawnmove :
	nomove ).

advice( promote_pawn,
	pawnpromoted :
	not pawnlost :
	(depth = 0) and pawnmove :
	nomove ).

advice( block_king,
	wk_on_critical :
	not pawnlost :
	(depth = 0) and kingdiagfirst :
	nomove ).

advice( move_to_critical,
	closertocritical :
	not pawnlost :
	(depth = 0) and kingdiagfirst :
	nomove ).

advice( any_move,
	closertopromotion or closertocritical :
	not pawnlost :
	(depth = 0) and (pawnmove or kingdiagfirst) :
	nomove ).

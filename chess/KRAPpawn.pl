% -*- Mode: Prolog -*-
% King and Pawn vs king in Advice Language 0

% all rules

square_rule :: if not bk_in_square or wk_on_critical
	then [ race_pawn ].
else_rule :: if true
	then [ block_king, race_pawn ].

% pieces of advice
% structure:
% advice( NAME, BETTERGOAL, HOLDINGGOAL: USMOVECONSTRAINT:
%		THEMMOVECONSTRAINT


advice( race_pawn,
	pawnpromoted,
	not pawnlost :
	pawnmove :
    legal ).

advice( block_king,
	wk_on_critical or not bk_in_square,
	not pawnlost :
	kingdiagfirst :
	legal ).


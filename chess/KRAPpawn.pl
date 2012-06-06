% -*- Mode: Prolog -*-
% King and Pawn vs king in Advice Language 0

% all rules

square_rule :: if king_in_square or king_blocked
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
	king_blocked or not king_in_square,
	not pawnlost :
	kingdiagfirst :
	legal ).


% -*- Mode: Prolog -*-
% $Id: KRAPqueen.pl,v 1.1 2004/05/31 19:47:25 mtjspaan Exp $

% King and Queen vs king in Advice Language 0

% all rules


else_rule :: if true
	then [ any_move ].

% pieces of advice
% structure:
% advice( NAME, BETTERGOAL, HOLDINGGOAL: USMOVECONSTRAINT: 
%		THEMMOVECONSTRAINT


advice( any_move, 
	not did_not_move_queen :
	not queenlost :
	(depth = 0) and queenmove :
        (depth = 1) and legal ).



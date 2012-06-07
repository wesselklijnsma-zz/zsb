% -*- Mode: Prolog -*-
% $Id: chess.pl,v 1.14 2008/05/28 22:56:56 obooij Exp $
% Small start up program


% initialize the operators needed for the advice language
:- discontiguous(move).
:- dynamic(move).
:- op( 200, xfy, [:, :: ] ).
:- op( 220, xfy, .. ).
:- op( 185, fx, if ).
:- op( 190, xfy, then ).
:- op( 180, xfy, or ).
:- op( 160, xfy, and ).
:- op( 140, fx, not ).

init :-
	consult('AL0'),
	consult('KRPL'),
	consult('readGnuChessrFile').

initRook:-
         init,!,
  write_ln('init done'),!,
  consult('KRPLrook'),
  write_ln('KRPLrook done'),!,
  consult('KRAProok'),
  write_ln('KRAProok done').
  
initRookrook:-
         init,!,
  write_ln('init done'),!,
  consult('KRPLrookrook'),
  write_ln('KRPLrookrook done'),!,
  consult('KRAProokrook'),
  write_ln('KRAProokrook done').

initQueen:-
      init,!,
  write_ln('init done'),!,
	consult('KRPLqueen'),!,
  write_ln('KRPLqueen done'),!,
  consult('KRAPqueen'),!,
  write_ln('KRAPqueen done').

initPawn:-
      init,!,
  write_ln('init done'),!,
	consult('KRPLpawn'),!,
  write_ln('KRPLpawn done'),!,
  consult('KRAPpawn'),!,
  write_ln('KRAPpawn done').

startRook :-
	initRook,
	playMove.
	
startRookrook :-
	initRookrook,
	playMove.

startQueen :-
	initQueen,
	playMove.

startPawn :-
	initPawn,
	playMove.
% move one time

playMove :-
  %leesUit( Positie ), 
  readGnuChessrFile( Positie ),
  write('Loaded the following position: '),
	write( Positie ), nl,
	% call AL0 playgame
  playgame( Positie ).

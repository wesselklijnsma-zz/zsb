% Tic-tac-toe by Jouke van der Maas (10186883) and Wessel Klijnsma (10172432)

:- consult('algorithms.pl').
:- consult('print.pl').

% Board representation:
% Actor/Board
%
% Where actor is either -1 or 1, depending on whose turn it is.
% Board is a flat list containing 1, -1 or 0s depending on its state.

% You can only move if the game is not finished. Use findall
% to find all possible moves and put them in a list.
moves(Pos, PosList) :-
    \+ staticval(Pos, _),
    findall(New, possible_move(Pos, New), PosList).

% Find an empty spot (there's a 0), change it to the actor
% and make it into a new list.
possible_move(Actor/Pos, NewActor/New) :-
    append(Start, [0|End], Pos),
    append(Start, [Actor|End], New),
    NewActor is Actor * -1.

% This is saved in the state, so it's easy to use.
min_to_move(-1/_).
max_to_move(1/_).

% Check's if there's a horizontal threesome
hor([A, A, A|_], A) :- A \= 0.
hor([_, _, _| Rest], Val) :-
    hor(Rest, Val).

% Check's if there's a vertical threesome
ver([A, _, _, A, _, _, A, _, _], A) :- A \= 0.
ver([_, A, _, _, A, _, _, A, _], A) :- A \= 0.
ver([_, _, A, _, _, A, _, _, A], A) :- A \= 0.

% Check's if there's a diagonal threesome
diag([A, _, _, _, A, _, _, _, A], A) :- A \= 0.
diag([_, _, A, _, A, _, A, _, _], A) :- A \= 0.

% Check's if the game is a tie (board is full)
tie([], 0).
tie([H|T], 0):-
    H \= 0,
    tie(T, 0).

staticval(_/Pos, Val) :-
    diag(Pos, Val);
    hor(Pos, Val);
    ver(Pos, Val);
    tie(Pos, Val).

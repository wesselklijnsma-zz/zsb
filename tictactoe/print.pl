% printing boards by Jouke van der Maas and Wessel Klijnsma

print_board(_/[]) :- print_line, !.

% Print the items, three at a time.
print_board(X/[S1, S2, S3 | Rest]) :-
    print_line,
    print_item(S1),
    print_item(S2),
    print_item(S3),
    write('| \n'),
    print_board(X/Rest).

% The size of the board is fixed, so lines are
% easy.
print_line :-
    write_ln('+---+---+---+').

% Print the item depending on
print_item(1) :-
    write('| X ').
print_item(-1) :-
    write('| O ').
print_item(0) :-
    write('|   ').
